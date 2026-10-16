ALTER TABLE category_budgets DROP FOREIGN KEY fk_category_budgets_category;
ALTER TABLE category_budgets MODIFY COLUMN category_id CHAR(36) NOT NULL;
ALTER TABLE category_budgets MODIFY COLUMN owner_id CHAR(36) NOT NULL;
ALTER TABLE category_budgets ADD CONSTRAINT fk_category_budgets_category FOREIGN KEY (category_id) REFERENCES categories(id);
