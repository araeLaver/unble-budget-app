const { Client } = require('pg');

const client = new Client({
  host: 'ep-blue-unit-a2ev3s9x.eu-central-1.pg.koyeb.app',
  port: 5432,
  database: 'untab',
  user: 'untab',
  password: '0AbVNOIsl2dn',
  ssl: { rejectUnauthorized: false }
});

async function checkSchemas() {
  try {
    console.log('🔍 데이터베이스 연결 중...');
    await client.connect();
    console.log('✅ 연결 성공!');

    // 현재 모든 스키마 확인
    console.log('\n📊 현재 모든 스키마:');
    const allSchemas = await client.query(`
      SELECT nspname as schemaname 
      FROM pg_namespace 
      WHERE nspname NOT IN ('information_schema', 'pg_catalog', 'pg_toast') 
      ORDER BY nspname
    `);
    
    allSchemas.rows.forEach(row => {
      console.log(`   - ${row.schemaname}`);
    });

    // 특정 스키마들 상세 확인
    console.log('\n🔍 관련 스키마 상세 확인:');
    const targetSchemas = ['dev_schema', 'unble_budget_dev', 'unble_dev', 'unble_prod'];
    
    for (const schema of targetSchemas) {
      const schemaExists = await client.query(
        'SELECT COUNT(*) FROM pg_namespace WHERE nspname = $1', 
        [schema]
      );
      
      if (schemaExists.rows[0].count > 0) {
        console.log(`   ✅ ${schema} - 존재함`);
        
        // 해당 스키마의 테이블 목록
        const tables = await client.query(
          'SELECT tablename FROM pg_tables WHERE schemaname = $1 ORDER BY tablename', 
          [schema]
        );
        
        if (tables.rows.length > 0) {
          console.log(`      테이블: ${tables.rows.map(t => t.tablename).join(', ')}`);
        } else {
          console.log(`      테이블: 없음`);
        }
      } else {
        console.log(`   ❌ ${schema} - 존재하지 않음`);
      }
    }

    // dev_schema가 존재한다면 삭제
    const devSchemaExists = await client.query(
      'SELECT COUNT(*) FROM pg_namespace WHERE nspname = $1', 
      ['dev_schema']
    );
    
    if (devSchemaExists.rows[0].count > 0) {
      console.log('\n🗑️  dev_schema가 다시 생성되어 있습니다. 삭제합니다...');
      await client.query('DROP SCHEMA IF EXISTS dev_schema CASCADE');
      console.log('✅ dev_schema 삭제 완료');
    }

    // unble_dev 스키마가 제대로 있는지 확인
    const unbleDevExists = await client.query(
      'SELECT COUNT(*) FROM pg_namespace WHERE nspname = $1', 
      ['unble_dev']
    );
    
    if (unbleDevExists.rows[0].count === 0) {
      console.log('\n🚀 unble_dev 스키마를 생성합니다...');
      await client.query('CREATE SCHEMA IF NOT EXISTS unble_dev');
      await client.query('GRANT ALL PRIVILEGES ON SCHEMA unble_dev TO untab');
      console.log('✅ unble_dev 스키마 생성 완료');
    }

    console.log('\n🎯 최종 스키마 상태:');
    const finalSchemas = await client.query(`
      SELECT nspname as schemaname 
      FROM pg_namespace 
      WHERE nspname LIKE '%unble%' OR nspname LIKE '%dev%' OR nspname LIKE '%budget%'
      ORDER BY nspname
    `);
    
    finalSchemas.rows.forEach(row => {
      console.log(`   ✓ ${row.schemaname}`);
    });

  } catch (error) {
    console.error('❌ 오류 발생:', error.message);
  } finally {
    await client.end();
  }
}

checkSchemas();