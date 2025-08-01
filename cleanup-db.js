const { Client } = require('pg');

// Database connection
const client = new Client({
  host: 'ep-blue-unit-a2ev3s9x.eu-central-1.pg.koyeb.app',
  port: 5432,
  database: 'untab',
  user: 'untab',
  password: '0AbVNOIsl2dn',
  ssl: { rejectUnauthorized: false }
});

async function cleanupDatabase() {
  try {
    console.log('🔍 Connecting to database...');
    await client.connect();
    console.log('✅ Connected successfully!');

    // 1. Check current schemas
    console.log('\n📊 Current schemas:');
    const schemasResult = await client.query(`
      SELECT nspname as schemaname FROM pg_namespace 
      WHERE nspname LIKE '%unble%' OR nspname LIKE '%dev%' OR nspname LIKE '%budget%'
      ORDER BY nspname
    `);
    
    schemasResult.rows.forEach(row => {
      console.log(`  - ${row.schemaname}`);
    });

    // 2. Check tables in each schema
    console.log('\n📋 Tables in each schema:');
    const schemas = ['dev_schema', 'unble_budget_dev', 'unble_dev'];
    
    for (const schema of schemas) {
      try {
        const tablesResult = await client.query(`
          SELECT tablename FROM pg_tables 
          WHERE schemaname = $1 
          ORDER BY tablename
        `, [schema]);
        
        if (tablesResult.rows.length > 0) {
          console.log(`\n🗂️  ${schema}:`);
          tablesResult.rows.forEach(row => {
            console.log(`     - ${row.tablename}`);
          });
        }
      } catch (e) {
        console.log(`   - ${schema}: not found`);
      }
    }

    // 3. Cleanup unnecessary schemas
    console.log('\n🗑️  Cleaning up unnecessary schemas...');
    
    // Drop dev_schema
    try {
      await client.query('DROP SCHEMA IF EXISTS dev_schema CASCADE');
      console.log('✅ Removed dev_schema');
    } catch (e) {
      console.log('- dev_schema not found or already removed');
    }
    
    // Drop unble_budget_dev
    try {
      await client.query('DROP SCHEMA IF EXISTS unble_budget_dev CASCADE');
      console.log('✅ Removed unble_budget_dev');
    } catch (e) {
      console.log('- unble_budget_dev not found or already removed');
    }

    // 4. Create production schema
    console.log('\n🚀 Creating production schema...');
    await client.query('CREATE SCHEMA IF NOT EXISTS unble_prod');
    console.log('✅ Created unble_prod schema');
    
    await client.query('GRANT ALL PRIVILEGES ON SCHEMA unble_prod TO untab');
    console.log('✅ Granted privileges to untab user');

    // 5. Final check
    console.log('\n✅ Final schema state:');
    const finalResult = await client.query(`
      SELECT nspname as schemaname FROM pg_namespace 
      WHERE nspname IN ('unble_dev', 'unble_prod')
      ORDER BY nspname
    `);
    
    finalResult.rows.forEach(row => {
      console.log(`  ✓ ${row.schemaname} (final)`);
    });

    console.log('\n🎯 Database cleanup completed successfully!');
    console.log('📊 Result: Only unble_dev and unble_prod schemas remain');

  } catch (error) {
    console.error('❌ Error:', error.message);
  } finally {
    await client.end();
  }
}

cleanupDatabase();