package com.ddoongddak.promeditor.config;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategySnakeCaseImpl;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

public class UpperCaseNamingStrategy extends PhysicalNamingStrategySnakeCaseImpl {

    @Override
    public Identifier toPhysicalTableName(Identifier logicalName, JdbcEnvironment jdbcEnvironment) {
        Identifier id = super.toPhysicalTableName(logicalName, jdbcEnvironment);
        return id == null ? null : Identifier.toIdentifier(id.getText().toUpperCase(), id.isQuoted());
    }

    @Override
    public Identifier toPhysicalColumnName(Identifier logicalName, JdbcEnvironment jdbcEnvironment) {
        Identifier id = super.toPhysicalColumnName(logicalName, jdbcEnvironment);
        return id == null ? null : Identifier.toIdentifier(id.getText().toUpperCase(), id.isQuoted());
    }
}
