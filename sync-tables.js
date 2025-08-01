const { Client } = require('pg');

const client = new Client({
  host: 'ep-blue-unit-a2ev3s9x.eu-central-1.pg.koyeb.app',
  port: 5432,
  database: 'untab',
  user: 'untab',
  password: '0AbVNOIsl2dn',
  ssl: { rejectUnauthorized: false }
});

async function syncTables() {
  try {
    console.log('🔍 Connecting to database...');
    await client.connect();
    console.log('✅ Connected successfully!');

    // Create all tables in unble_prod schema identical to unble_dev
    console.log('\n🚀 Creating tables in unble_prod schema...');

    // Users table
    await client.query(`
      CREATE TABLE IF NOT EXISTS unble_prod.users (
        id BIGSERIAL PRIMARY KEY,
        email VARCHAR(255) NOT NULL UNIQUE,
        password VARCHAR(255) NOT NULL,
        name VARCHAR(100) NOT NULL,
        is_active BOOLEAN DEFAULT true,
        last_login_at TIMESTAMP,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
      )
    `);
    console.log('✅ Created users table');

    // Categories table
    await client.query(`
      CREATE TABLE IF NOT EXISTS unble_prod.categories (
        id BIGSERIAL PRIMARY KEY,
        name VARCHAR(100) NOT NULL,
        color VARCHAR(7) DEFAULT '#000000',
        icon VARCHAR(50) DEFAULT '💰',
        category_type VARCHAR(20) DEFAULT 'EXPENSE' CHECK (category_type IN ('INCOME', 'EXPENSE', 'ASSET')),
        is_default BOOLEAN DEFAULT false,
        sort_order INTEGER DEFAULT 0,
        user_id BIGINT,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (user_id) REFERENCES unble_prod.users(id) ON DELETE CASCADE
      )
    `);
    console.log('✅ Created categories table');

    // Transactions table
    await client.query(`
      CREATE TABLE IF NOT EXISTS unble_prod.transactions (
        id BIGSERIAL PRIMARY KEY,
        user_id BIGINT NOT NULL,
        category_id BIGINT NOT NULL,
        amount DECIMAL(15,2) NOT NULL,
        description TEXT,
        transaction_type VARCHAR(20) NOT NULL CHECK (transaction_type IN ('INCOME', 'EXPENSE')),
        transaction_date DATE NOT NULL,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (user_id) REFERENCES unble_prod.users(id) ON DELETE CASCADE,
        FOREIGN KEY (category_id) REFERENCES unble_prod.categories(id) ON DELETE RESTRICT
      )
    `);
    console.log('✅ Created transactions table');

    // User Settings table
    await client.query(`
      CREATE TABLE IF NOT EXISTS unble_prod.user_settings (
        id BIGSERIAL PRIMARY KEY,
        user_id BIGINT NOT NULL UNIQUE,
        currency VARCHAR(3) DEFAULT 'KRW',
        theme VARCHAR(20) DEFAULT 'light',
        notification_enabled BOOLEAN DEFAULT true,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (user_id) REFERENCES unble_prod.users(id) ON DELETE CASCADE
      )
    `);
    console.log('✅ Created user_settings table');

    // Budget Plans table
    await client.query(`
      CREATE TABLE IF NOT EXISTS unble_prod.budget_plans (
        id BIGSERIAL PRIMARY KEY,
        user_id BIGINT NOT NULL,
        category_id BIGINT NOT NULL,
        budget_month DATE NOT NULL,
        planned_amount DECIMAL(15,2) NOT NULL,
        actual_amount DECIMAL(15,2) DEFAULT 0,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (user_id) REFERENCES unble_prod.users(id) ON DELETE CASCADE,
        FOREIGN KEY (category_id) REFERENCES unble_prod.categories(id) ON DELETE CASCADE,
        UNIQUE (user_id, category_id, budget_month)
      )
    `);
    console.log('✅ Created budget_plans table');

    // Create indexes for performance
    console.log('\n🔧 Creating indexes...');
    
    const indexes = [
      'CREATE INDEX IF NOT EXISTS idx_users_email ON unble_prod.users(email)',
      'CREATE INDEX IF NOT EXISTS idx_categories_user_id ON unble_prod.categories(user_id)',
      'CREATE INDEX IF NOT EXISTS idx_transactions_user_id ON unble_prod.transactions(user_id)',
      'CREATE INDEX IF NOT EXISTS idx_transactions_date ON unble_prod.transactions(transaction_date)',
      'CREATE INDEX IF NOT EXISTS idx_budget_user_month ON unble_prod.budget_plans(user_id, budget_month)'
    ];

    for (const indexQuery of indexes) {
      await client.query(indexQuery);
    }
    console.log('✅ Created performance indexes');

    // Insert default categories
    console.log('\n🏷️  Inserting default categories...');
    await client.query(`
      INSERT INTO unble_prod.categories (name, color, icon, category_type, is_default, sort_order) VALUES
      -- Assets
      ('현금', '#2ECC71', '💰', 'ASSET', true, 1),
      ('은행예금', '#3498DB', '🏦', 'ASSET', true, 2),
      ('적금', '#9B59B6', '💎', 'ASSET', true, 3),
      ('투자', '#E74C3C', '📈', 'ASSET', true, 4),
      ('카드잔액', '#F39C12', '💳', 'ASSET', true, 5),
      
      -- Expenses
      ('식비', '#E67E22', '🍽️', 'EXPENSE', true, 10),
      ('교통비', '#3498DB', '🚗', 'EXPENSE', true, 11),
      ('생활용품', '#95A5A6', '🛒', 'EXPENSE', true, 12),
      ('의료비', '#E74C3C', '🏥', 'EXPENSE', true, 13),
      ('문화생활', '#9B59B6', '🎬', 'EXPENSE', true, 14),
      ('통신비', '#34495E', '📱', 'EXPENSE', true, 15),
      ('주거비', '#16A085', '🏠', 'EXPENSE', true, 16),
      ('보험료', '#2980B9', '🛡️', 'EXPENSE', true, 17),
      ('교육비', '#8E44AD', '📚', 'EXPENSE', true, 18),
      ('기타지출', '#BDC3C7', '💸', 'EXPENSE', true, 19),
      
      -- Income
      ('급여', '#27AE60', '💼', 'INCOME', true, 20),
      ('부업', '#F1C40F', '💻', 'INCOME', true, 21),
      ('투자수익', '#E74C3C', '📊', 'INCOME', true, 22),
      ('기타수입', '#95A5A6', '💰', 'INCOME', true, 23)
      
    `);
    console.log('✅ Inserted 23 default categories');

    // Grant all privileges
    await client.query('GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA unble_prod TO untab');
    await client.query('GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA unble_prod TO untab');
    console.log('✅ Granted all privileges');

    // Final verification
    console.log('\n✅ Final verification:');
    
    // Check dev schema tables
    const devTables = await client.query(`
      SELECT tablename FROM pg_tables WHERE schemaname = 'unble_dev' ORDER BY tablename
    `);
    console.log(`📊 unble_dev tables: ${devTables.rows.length}`);
    devTables.rows.forEach(row => console.log(`     - ${row.tablename}`));

    // Check prod schema tables  
    const prodTables = await client.query(`
      SELECT tablename FROM pg_tables WHERE schemaname = 'unble_prod' ORDER BY tablename
    `);
    console.log(`📊 unble_prod tables: ${prodTables.rows.length}`);
    prodTables.rows.forEach(row => console.log(`     - ${row.tablename}`));

    // Check categories count
    const devCategoriesCount = await client.query('SELECT COUNT(*) FROM unble_dev.categories WHERE is_default = true');
    const prodCategoriesCount = await client.query('SELECT COUNT(*) FROM unble_prod.categories WHERE is_default = true');
    
    console.log(`🏷️  Default categories - Dev: ${devCategoriesCount.rows[0].count}, Prod: ${prodCategoriesCount.rows[0].count}`);

    console.log('\n🎯 Table synchronization completed successfully!');
    console.log('📊 Both unble_dev and unble_prod now have identical structure!');

  } catch (error) {
    console.error('❌ Error:', error.message);
  } finally {
    await client.end();
  }
}

syncTables();