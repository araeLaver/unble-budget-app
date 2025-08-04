const { Client } = require('pg');

// Database configuration
const dbConfigs = [
    {
        name: 'unble_dev (개발용)',
        schema: 'unble_dev',
        config: {
            host: 'ep-blue-unit-a2ev3s9x.eu-central-1.pg.koyeb.app',
            port: 5432,
            database: 'untab',
            user: 'untab',
            password: '0AbVNOIsl2dn',
            ssl: { rejectUnauthorized: false }
        }
    },
    {
        name: 'unble_prod (운영용)',
        schema: 'unble_prod',
        config: {
            host: 'ep-blue-unit-a2ev3s9x.eu-central-1.pg.koyeb.app',
            port: 5432,
            database: 'untab',
            user: 'untab',
            password: '0AbVNOIsl2dn',
            ssl: { rejectUnauthorized: false }
        }
    }
];

async function updateConstraints() {
    console.log('🚀 ASSET 제약조건 업데이트를 시작합니다...\n');

    for (const { name, schema, config } of dbConfigs) {
        console.log(`📊 ${name} 업데이트 중...`);
        
        const client = new Client(config);
        
        try {
            await client.connect();
            console.log(`✅ ${name} 연결 성공`);
            
            // Set schema
            await client.query(`SET search_path TO ${schema}`);
            
            // Check if transactions table exists
            const tableCheck = await client.query(`
                SELECT table_name 
                FROM information_schema.tables 
                WHERE table_schema = '${schema}' AND table_name = 'transactions'
            `);
            
            if (tableCheck.rows.length === 0) {
                console.log(`⚠️ ${name}에서 transactions 테이블을 찾을 수 없습니다.`);
                continue;
            }
            
            // Drop existing constraint
            console.log(`  - 기존 제약조건 삭제 중...`);
            await client.query(`
                ALTER TABLE transactions 
                DROP CONSTRAINT IF EXISTS transactions_transaction_type_check
            `);
            
            // Add new constraint with ASSET
            console.log(`  - ASSET을 포함한 새 제약조건 추가 중...`);
            await client.query(`
                ALTER TABLE transactions 
                ADD CONSTRAINT transactions_transaction_type_check 
                CHECK (transaction_type IN ('INCOME', 'EXPENSE', 'ASSET'))
            `);
            
            // Verify constraint
            const constraintCheck = await client.query(`
                SELECT constraint_name, check_clause 
                FROM information_schema.check_constraints 
                WHERE constraint_name = 'transactions_transaction_type_check'
            `);
            
            if (constraintCheck.rows.length > 0) {
                console.log(`✅ ${name} ASSET 제약조건 적용 완료!`);
                console.log(`   제약조건: ${constraintCheck.rows[0].check_clause}`);
            } else {
                console.log(`⚠️ ${name} 제약조건 확인 실패`);
            }
            
        } catch (error) {
            console.error(`❌ ${name} 업데이트 실패:`, error.message);
        } finally {
            await client.end();
        }
        
        console.log('');
    }
    
    console.log('🎉 모든 데이터베이스 업데이트가 완료되었습니다!');
    console.log('이제 자산 관리 기능을 사용할 수 있습니다. 🎊');
}

// Run the update
updateConstraints().catch(console.error);