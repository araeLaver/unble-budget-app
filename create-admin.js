const { Client } = require('pg');

const client = new Client({
  host: 'ep-blue-unit-a2ev3s9x.eu-central-1.pg.koyeb.app',
  port: 5432,
  database: 'untab',
  user: 'untab',
  password: '0AbVNOIsl2dn',
  ssl: { rejectUnauthorized: false }
});

async function createAdminAccount() {
  try {
    console.log('🔍 데이터베이스에 연결 중...');
    await client.connect();
    console.log('✅ 연결 성공!');

    // 현재 users 테이블 확인
    console.log('\n📊 현재 사용자 목록:');
    const usersResult = await client.query('SELECT id, email, name, is_active FROM unble_dev.users ORDER BY id');
    
    if (usersResult.rows.length === 0) {
      console.log('   (사용자가 없습니다)');
    } else {
      usersResult.rows.forEach(user => {
        console.log(`   - ${user.email} (${user.name}) - ${user.is_active ? '활성' : '비활성'}`);
      });
    }

    // admin 계정 존재 확인
    const adminCheck = await client.query(
      'SELECT * FROM unble_dev.users WHERE email = $1', 
      ['admin@unble.com']
    );

    if (adminCheck.rows.length > 0) {
      console.log('\n⚠️  admin 계정이 이미 존재합니다.');
      const admin = adminCheck.rows[0];
      console.log(`   이메일: ${admin.email}`);
      console.log(`   이름: ${admin.name}`);
      console.log(`   활성 상태: ${admin.is_active}`);
      
      // 기존 admin 계정 삭제 후 재생성
      console.log('\n🗑️  기존 admin 계정을 삭제하고 새로 생성합니다...');
      await client.query('DELETE FROM unble_dev.users WHERE email = $1', ['admin@unble.com']);
    }

    // bcrypt로 비밀번호 해싱 (Spring Boot와 동일한 방식)
    const bcrypt = require('bcrypt');
    const hashedPassword = bcrypt.hashSync('admin123', 10);

    // 새 admin 계정 생성
    console.log('\n🔐 새 admin 계정을 생성합니다...');
    const insertResult = await client.query(`
      INSERT INTO unble_dev.users (email, password, name, is_active, created_at, updated_at) 
      VALUES ($1, $2, $3, $4, NOW(), NOW()) 
      RETURNING id, email, name
    `, ['admin@unble.com', hashedPassword, 'Administrator', true]);

    const newAdmin = insertResult.rows[0];
    console.log('✅ admin 계정이 성공적으로 생성되었습니다!');
    console.log(`   ID: ${newAdmin.id}`);
    console.log(`   이메일: ${newAdmin.email}`);
    console.log(`   이름: ${newAdmin.name}`);
    console.log(`   비밀번호: admin123`);

    // 최종 확인
    console.log('\n📋 최종 사용자 목록:');
    const finalUsers = await client.query('SELECT id, email, name, is_active FROM unble_dev.users ORDER BY id');
    finalUsers.rows.forEach(user => {
      const isAdmin = user.email === 'admin@unble.com' ? ' 👑 (관리자)' : '';
      console.log(`   - ${user.email} (${user.name}) - ${user.is_active ? '활성' : '비활성'}${isAdmin}`);
    });

    console.log('\n🎯 이제 다음 정보로 로그인할 수 있습니다:');
    console.log('   이메일: admin@unble.com');
    console.log('   비밀번호: admin123');

  } catch (error) {
    console.error('❌ 오류 발생:', error.message);
  } finally {
    await client.end();
  }
}

createAdminAccount();