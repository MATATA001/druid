/*
 * Copyright 1999-2101 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.druid.sql;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.alibaba.druid.DruidRuntimeException;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLObject;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOperator;
import com.alibaba.druid.sql.ast.statement.SQLDeleteStatement;
import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
import com.alibaba.druid.sql.ast.statement.SQLSelectOrderByItem;
import com.alibaba.druid.sql.ast.statement.SQLSelectQuery;
import com.alibaba.druid.sql.ast.statement.SQLSelectQueryBlock;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.ast.statement.SQLSetStatement;
import com.alibaba.druid.sql.ast.statement.SQLUpdateSetItem;
import com.alibaba.druid.sql.ast.statement.SQLUpdateStatement;
import com.alibaba.druid.sql.dialect.db2.visitor.DB2OutputVisitor;
import com.alibaba.druid.sql.dialect.db2.visitor.DB2SchemaStatVisitor;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlOutputVisitor;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlSchemaStatVisitor;
import com.alibaba.druid.sql.dialect.odps.visitor.OdpsOutputVisitor;
import com.alibaba.druid.sql.dialect.odps.visitor.OdpsSchemaStatVisitor;
import com.alibaba.druid.sql.dialect.oracle.visitor.OracleOutputVisitor;
import com.alibaba.druid.sql.dialect.oracle.visitor.OracleSchemaStatVisitor;
import com.alibaba.druid.sql.dialect.oracle.visitor.OracleToMySqlOutputVisitor;
import com.alibaba.druid.sql.dialect.postgresql.visitor.PGOutputVisitor;
import com.alibaba.druid.sql.dialect.postgresql.visitor.PGSchemaStatVisitor;
import com.alibaba.druid.sql.dialect.sqlserver.visitor.SQLServerOutputVisitor;
import com.alibaba.druid.sql.dialect.sqlserver.visitor.SQLServerSchemaStatVisitor;
import com.alibaba.druid.sql.parser.*;
import com.alibaba.druid.sql.visitor.SQLASTOutputVisitor;
import com.alibaba.druid.sql.visitor.SchemaStatVisitor;
import com.alibaba.druid.support.logging.Log;
import com.alibaba.druid.support.logging.LogFactory;
import com.alibaba.druid.util.JdbcConstants;
import com.alibaba.druid.util.StringUtils;
import com.alibaba.druid.util.Utils;

public class SQLUtils {
    public static FormatOption DEFAULT_FORMAT_OPTION = new FormatOption();
    public static FormatOption DEFAULT_LCASE_FORMAT_OPTION = new FormatOption(false);

    private final static Log LOG = LogFactory.getLog(SQLUtils.class);

    public static String toSQLString(SQLObject sqlObject, String dbType) {
        return toSQLString(sqlObject, dbType, null);
    }
    
    public static String toSQLString(SQLObject sqlObject, String dbType, FormatOption option) {
        StringBuilder out = new StringBuilder();
        SQLASTOutputVisitor visitor = createOutputVisitor(out, dbType);
        
        if (option == null) {
            option = DEFAULT_FORMAT_OPTION;
        }
        visitor.setUppCase(option.isUppCase());
        
        sqlObject.accept(visitor);

        String sql = out.toString();
        return sql;
    }

    public static String toSQLString(SQLObject sqlObject) {
        StringBuilder out = new StringBuilder();
        sqlObject.accept(new SQLASTOutputVisitor(out));

        String sql = out.toString();
        return sql;
    }

    public static String toOdpsString(SQLObject sqlObject) {
        return toOdpsString(sqlObject, null);
    }
    
    public static String toOdpsString(SQLObject sqlObject, FormatOption option) {
        return toSQLString(sqlObject, JdbcConstants.ODPS, option);
    }
    
    public static String toMySqlString(SQLObject sqlObject) {
        return toMySqlString(sqlObject, null);
    }

    public static String toMySqlString(SQLObject sqlObject, FormatOption option) {
        return toSQLString(sqlObject, JdbcConstants.MYSQL, option);
    }

    public static SQLExpr toMySqlExpr(String sql) {
        return toSQLExpr(sql, JdbcConstants.MYSQL);
    }

    public static String formatMySql(String sql) {
        return format(sql, JdbcConstants.MYSQL);
    }
    
    public static String formatMySql(String sql, FormatOption option) {
        return format(sql, JdbcConstants.MYSQL, option);
    }

    public static String formatOracle(String sql) {
        return format(sql, JdbcConstants.ORACLE);
    }
    
    public static String formatOracle(String sql, FormatOption option) {
        return format(sql, JdbcConstants.ORACLE, option);
    }

    public static String formatOdps(String sql) {
        return format(sql, JdbcConstants.ODPS);
    }
    
    public static String formatOdps(String sql, FormatOption option) {
        return format(sql, JdbcConstants.ODPS, option);
    }

    public static String formatSQLServer(String sql) {
        return format(sql, JdbcConstants.SQL_SERVER);
    }

    public static String toOracleString(SQLObject sqlObject) {
        return toOracleString(sqlObject, null);
    }
    
    public static String toOracleString(SQLObject sqlObject, FormatOption option) {
        return toSQLString(sqlObject, JdbcConstants.ORACLE, option);
    }
    
    public static String toPGString(SQLObject sqlObject) {
        return toPGString(sqlObject, null);
    }

    public static String toPGString(SQLObject sqlObject, FormatOption option) {
        return toSQLString(sqlObject, JdbcConstants.POSTGRESQL, option);
    }

    public static String toDB2String(SQLObject sqlObject) {
        return toDB2String(sqlObject, null);
    }
    
    public static String toDB2String(SQLObject sqlObject, FormatOption option) {
        return toSQLString(sqlObject, JdbcConstants.DB2, option);
    }

    public static String toSQLServerString(SQLObject sqlObject) {
        return toSQLServerString(sqlObject, null);
    }
    
    public static String toSQLServerString(SQLObject sqlObject, FormatOption option) {
        return toSQLString(sqlObject, JdbcConstants.SQL_SERVER, option);
    }
    
    public static String formatPGSql(String sql, FormatOption option) {
        return format(sql, JdbcConstants.POSTGRESQL, option);
    }

    public static SQLExpr toSQLExpr(String sql, String dbType) {
        SQLExprParser parser = SQLParserUtils.createExprParser(sql, dbType);
        SQLExpr expr = parser.expr();

        if (parser.getLexer().token() != Token.EOF) {
            throw new ParserException("illegal sql expr : " + sql);
        }

        return expr;
    }
    
    public static SQLSelectOrderByItem toOrderByItem(String sql, String dbType) {
        SQLExprParser parser = SQLParserUtils.createExprParser(sql, dbType);
        SQLSelectOrderByItem orderByItem = parser.parseSelectOrderByItem();
        
        if (parser.getLexer().token() != Token.EOF) {
            throw new ParserException("illegal sql expr : " + sql);
        }
        
        return orderByItem;
    }
    
    public static SQLUpdateSetItem toUpdateSetItem(String sql, String dbType) {
        SQLExprParser parser = SQLParserUtils.createExprParser(sql, dbType);
        SQLUpdateSetItem updateSetItem = parser.parseUpdateSetItem();
        
        if (parser.getLexer().token() != Token.EOF) {
            throw new ParserException("illegal sql expr : " + sql);
        }
        
        return updateSetItem;
    }
    
    public static SQLSelectItem toSelectItem(String sql, String dbType) {
        SQLExprParser parser = SQLParserUtils.createExprParser(sql, dbType);
        SQLSelectItem selectItem = parser.parseSelectItem();

        if (parser.getLexer().token() != Token.EOF) {
            throw new ParserException("illegal sql expr : " + sql);
        }

        return selectItem;
    }

    public static List<SQLStatement> toStatementList(String sql, String dbType) {
        SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sql, dbType);
        return parser.parseStatementList();
    }

    public static SQLExpr toSQLExpr(String sql) {
        return toSQLExpr(sql, null);
    }

    public static String format(String sql, String dbType) {
        return format(sql, dbType, null, null);
    }
    
    public static String format(String sql, String dbType, FormatOption option) {
        return format(sql, dbType, null, option);
    }
    
    public static String format(String sql, String dbType, List<Object> parameters) {
        return format(sql, dbType, parameters, null);
    }

    public static String format(String sql, String dbType, List<Object> parameters, FormatOption option) {
        try {
            SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sql, dbType);
            parser.setKeepComments(true);
            
            List<SQLStatement> statementList = parser.parseStatementList();

            return toSQLString(statementList, dbType, parameters, option);
        } catch (ParserException ex) {
            LOG.warn("format error", ex);
            return sql;
        }
    }

    public static String toSQLString(List<SQLStatement> statementList, String dbType) {
        return toSQLString(statementList, dbType, (List<Object>) null);
    }
    
    public static String toSQLString(List<SQLStatement> statementList, String dbType, FormatOption option) {
        return toSQLString(statementList, dbType, null, option);
    }
    
    public static String toSQLString(List<SQLStatement> statementList, String dbType, List<Object> parameters) {
        return toSQLString(statementList, dbType, parameters, null, null);
    }

    public static String toSQLString(List<SQLStatement> statementList, String dbType, List<Object> parameters, FormatOption option) {
        return toSQLString(statementList, dbType, parameters, option, null);
    }

    public static String toSQLString(List<SQLStatement> statementList
            , String dbType
            , List<Object> parameters
            , FormatOption option
            , Map<String, String> tableMapping) {
        StringBuilder out = new StringBuilder();
        SQLASTOutputVisitor visitor = createFormatOutputVisitor(out, statementList, dbType);
        if (parameters != null) {
            visitor.setParameters(parameters);
        }
        
        if (option == null) {
            option = DEFAULT_FORMAT_OPTION;
        }
        visitor.setUppCase(option.isUppCase());

        if (tableMapping != null) {
            visitor.setTableMapping(tableMapping);
        }

        for (int i = 0; i < statementList.size(); i++) {
            SQLStatement stmt = statementList.get(i);
            
            if (i > 0) {
                visitor.print(";");
                
                SQLStatement preStmt = statementList.get(i - 1);
                List<String> comments = preStmt.getAfterCommentsDirect();
                if (comments != null){
                    for (int j = 0; j < comments.size(); ++j) {
                        String comment = comments.get(j);
                        if (j != 0) {
                            visitor.println();
                        }
                        visitor.print(comment);
                    }
                }
                visitor.println();
                
                if (!(stmt instanceof SQLSetStatement)) {
                    visitor.println();
                }
            }
            {
                List<String> comments = stmt.getBeforeCommentsDirect();
                if (comments != null){
                    for(String comment : comments) {
                        visitor.println(comment);
                    }
                }
            }
            stmt.accept(visitor);
            
            if (i == statementList.size() - 1) {
                Boolean semi = (Boolean) stmt.getAttribute("format.semi");
                if (semi != null && semi.booleanValue()) {
//                    if (stmt.hasAfterComment()) {
//                        visitor.println();
//                    }
                    visitor.print(";");
                }
                
                List<String> comments = stmt.getAfterCommentsDirect();
                if (comments != null){
                    for (int j = 0; j < comments.size(); ++j) {
                        String comment = comments.get(j);
                        if (j != 0) {
                            visitor.println();
                        }
                        visitor.print(comment);
                    }
                }
            }
        }

        return out.toString();
    }
    
    public static SQLASTOutputVisitor createOutputVisitor(Appendable out, String dbType) {
        return createFormatOutputVisitor(out, null, dbType);
    }

    public static SQLASTOutputVisitor createFormatOutputVisitor(Appendable out, // 
                                                                List<SQLStatement> statementList, //
                                                                String dbType) {
        if (JdbcConstants.ORACLE.equals(dbType) || JdbcConstants.ALI_ORACLE.equals(dbType)) {
            if (statementList == null || statementList.size() == 1) {
                return new OracleOutputVisitor(out, false);
            } else {
                return new OracleOutputVisitor(out, true);
            }
        }

        if (JdbcConstants.MYSQL.equals(dbType) //
            || JdbcConstants.MARIADB.equals(dbType) //
            || JdbcConstants.H2.equals(dbType)) {
            return new MySqlOutputVisitor(out);
        }

        if (JdbcConstants.POSTGRESQL.equals(dbType)) {
            return new PGOutputVisitor(out);
        }

        if (JdbcConstants.SQL_SERVER.equals(dbType) || JdbcConstants.JTDS.equals(dbType)) {
            return new SQLServerOutputVisitor(out);
        }

        if (JdbcConstants.DB2.equals(dbType)) {
            return new DB2OutputVisitor(out);
        }

        if (JdbcConstants.ODPS.equals(dbType)) {
            return new OdpsOutputVisitor(out);
        }
        
        return new SQLASTOutputVisitor(out, dbType);
    }
    
    @Deprecated
    public static SchemaStatVisitor createSchemaStatVisitor(List<SQLStatement> statementList, String dbType) {
        return createSchemaStatVisitor(dbType);
    }

    public static SchemaStatVisitor createSchemaStatVisitor(String dbType) {
        if (JdbcConstants.ORACLE.equals(dbType) || JdbcConstants.ALI_ORACLE.equals(dbType)) {
            return new OracleSchemaStatVisitor();
        }

        if (JdbcConstants.MYSQL.equals(dbType) || //
            JdbcConstants.MARIADB.equals(dbType) || //
            JdbcConstants.H2.equals(dbType)) {
            return new MySqlSchemaStatVisitor();
        }

        if (JdbcConstants.POSTGRESQL.equals(dbType)) {
            return new PGSchemaStatVisitor();
        }

        if (JdbcConstants.SQL_SERVER.equals(dbType) || JdbcConstants.JTDS.equals(dbType)) {
            return new SQLServerSchemaStatVisitor();
        }

        if (JdbcConstants.DB2.equals(dbType)) {
            return new DB2SchemaStatVisitor();
        }
        
        if (JdbcConstants.ODPS.equals(dbType)) {
            return new OdpsSchemaStatVisitor();
        }

        return new SchemaStatVisitor();
    }

    public static List<SQLStatement> parseStatements(String sql, String dbType) {
        SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sql, dbType);
        List<SQLStatement> stmtList = parser.parseStatementList();
        if (parser.getLexer().token() != Token.EOF) {
            throw new DruidRuntimeException("syntax error : " + sql);
        }
        return stmtList;
    }

    /**
     * @author owenludong.lud
     * @param columnName
     * @param tableAlias
     * @param pattern if pattern is null,it will be set {%Y-%m-%d %H:%i:%s} as mysql default value and set {yyyy-mm-dd
     * hh24:mi:ss} as oracle default value
     * @param dbType {@link JdbcConstants} if dbType is null ,it will be set the mysql as a default value
     */
    public static String buildToDate(String columnName, String tableAlias, String pattern, String dbType) {
        StringBuilder sql = new StringBuilder();
        if (StringUtils.isEmpty(columnName)) return "";
        if (StringUtils.isEmpty(dbType)) dbType = JdbcConstants.MYSQL;
        String formatMethod = "";
        if (JdbcConstants.MYSQL.equalsIgnoreCase(dbType)) {
            formatMethod = "STR_TO_DATE";
            if (StringUtils.isEmpty(pattern)) pattern = "%Y-%m-%d %H:%i:%s";
        } else if (JdbcConstants.ORACLE.equalsIgnoreCase(dbType)) {
            formatMethod = "TO_DATE";
            if (StringUtils.isEmpty(pattern)) pattern = "yyyy-mm-dd hh24:mi:ss";
        } else {
            return "";
            // expand date's handle method for other database
        }
        sql.append(formatMethod).append("(");
        if (!StringUtils.isEmpty(tableAlias)) sql.append(tableAlias).append(".");
        sql.append(columnName).append(",");
        sql.append("'");
        sql.append(pattern);
        sql.append("')");
        return sql.toString();
    }

    public static List<SQLExpr> split(SQLBinaryOpExpr x) {
        List<SQLExpr> groupList = new ArrayList<SQLExpr>();
        groupList.add(x.getRight());

        SQLExpr left = x.getLeft();
        for (;;) {
            if (left instanceof SQLBinaryOpExpr && ((SQLBinaryOpExpr) left).getOperator() == x.getOperator()) {
                SQLBinaryOpExpr binaryLeft = (SQLBinaryOpExpr) left;
                groupList.add(binaryLeft.getRight());
                left = binaryLeft.getLeft();
            } else {
                groupList.add(left);
                break;
            }
        }
        return groupList;
    }

    public static String translateOracleToMySql(String sql) {
        List<SQLStatement> stmtList = toStatementList(sql, JdbcConstants.ORACLE);

        StringBuilder out = new StringBuilder();
        OracleToMySqlOutputVisitor visitor = new OracleToMySqlOutputVisitor(out, false);
        for (int i = 0; i < stmtList.size(); ++i) {
            stmtList.get(i).accept(visitor);
        }

        String mysqlSql = out.toString();
        return mysqlSql;

    }

    public static String addCondition(String sql, String condition, String dbType) {
        String result = addCondition(sql, condition, SQLBinaryOperator.BooleanAnd, false, dbType);
        return result;
    }

    public static String addCondition(String sql, String condition, SQLBinaryOperator op, boolean left, String dbType) {
        if (sql == null) {
            throw new IllegalArgumentException("sql is null");
        }

        if (condition == null) {
            return sql;
        }

        if (op == null) {
            op = SQLBinaryOperator.BooleanAnd;
        }

        if (op != SQLBinaryOperator.BooleanAnd //
            && op != SQLBinaryOperator.BooleanOr) {
            throw new IllegalArgumentException("add condition not support : " + op);
        }

        List<SQLStatement> stmtList = parseStatements(sql, dbType);

        if (stmtList.size() == 0) {
            throw new IllegalArgumentException("not support empty-statement :" + sql);
        }

        if (stmtList.size() > 1) {
            throw new IllegalArgumentException("not support multi-statement :" + sql);
        }

        SQLStatement stmt = stmtList.get(0);

        SQLExpr conditionExpr = toSQLExpr(condition, dbType);

        addCondition(stmt, op, conditionExpr, left);

        return toSQLString(stmt, dbType);
    }

    public static void addCondition(SQLStatement stmt, SQLBinaryOperator op, SQLExpr condition, boolean left) {
        if (stmt instanceof SQLSelectStatement) {
            SQLSelectQuery query = ((SQLSelectStatement) stmt).getSelect().getQuery();
            if (query instanceof SQLSelectQueryBlock) {
                SQLSelectQueryBlock queryBlock = (SQLSelectQueryBlock) query;
                SQLExpr newCondition = buildCondition(op, condition, left, queryBlock.getWhere());
                queryBlock.setWhere(newCondition);
            } else {
                throw new IllegalArgumentException("add condition not support " + stmt.getClass().getName());
            }

            return;
        }

        if (stmt instanceof SQLDeleteStatement) {
            SQLDeleteStatement delete = (SQLDeleteStatement) stmt;

            SQLExpr newCondition = buildCondition(op, condition, left, delete.getWhere());
            delete.setWhere(newCondition);

            return;
        }

        if (stmt instanceof SQLUpdateStatement) {
            SQLUpdateStatement update = (SQLUpdateStatement) stmt;

            SQLExpr newCondition = buildCondition(op, condition, left, update.getWhere());
            update.setWhere(newCondition);

            return;
        }

        throw new IllegalArgumentException("add condition not support " + stmt.getClass().getName());
    }
    
    public static SQLExpr buildCondition(SQLBinaryOperator op, SQLExpr condition, boolean left, SQLExpr where) {
        if (where == null) {
            return condition;
        }

        SQLBinaryOpExpr newCondition;
        if (left) {
            newCondition = new SQLBinaryOpExpr(condition, op, where);            
        } else {
            newCondition = new SQLBinaryOpExpr(where, op, condition);
        }
        return newCondition;
    }
   
    public static String addSelectItem(String selectSql, String expr, String alias, String dbType) {
        return addSelectItem(selectSql, expr, alias, false, dbType);
    }
                                       
    public static String addSelectItem(String selectSql, String expr, String alias, boolean first, String dbType) {
        List<SQLStatement> stmtList = parseStatements(selectSql, dbType);

        if (stmtList.size() == 0) {
            throw new IllegalArgumentException("not support empty-statement :" + selectSql);
        }

        if (stmtList.size() > 1) {
            throw new IllegalArgumentException("not support multi-statement :" + selectSql);
        }

        SQLStatement stmt = stmtList.get(0);

        SQLExpr columnExpr = toSQLExpr(expr, dbType);

        addSelectItem(stmt, columnExpr, alias, first);

        return toSQLString(stmt, dbType);
    }
    
    public static void addSelectItem(SQLStatement stmt, SQLExpr expr, String alias, boolean first) {
        if (expr == null) {
            return;
        }
        
        if (stmt instanceof SQLSelectStatement) {
            SQLSelectQuery query = ((SQLSelectStatement) stmt).getSelect().getQuery();
            if (query instanceof SQLSelectQueryBlock) {
                SQLSelectQueryBlock queryBlock = (SQLSelectQueryBlock) query;
                addSelectItem(queryBlock, expr, alias, first);
            } else {
                throw new IllegalArgumentException("add condition not support " + stmt.getClass().getName());
            }

            return;
        }
        
        throw new IllegalArgumentException("add selectItem not support " + stmt.getClass().getName());
    }
    
    public static void addSelectItem(SQLSelectQueryBlock queryBlock, SQLExpr expr, String alias, boolean first) {
        SQLSelectItem selectItem = new SQLSelectItem(expr, alias);
        queryBlock.getSelectList().add(selectItem);
        selectItem.setParent(selectItem);
    }
    
    public static class FormatOption {

        private boolean ucase = true;
        
        public FormatOption() {
            
        }
        
        public FormatOption(boolean ucase) {
            this.ucase = ucase;
        }

        public boolean isUppCase() {
            return ucase;
        }

        public void setUppCase(boolean val) {
            this.ucase = val;
        }
    }

    public static String refactor(String sql, String dbType, Map<String, String> tableMapping) {
        List<SQLStatement> stmtList = parseStatements(sql, dbType);
        return SQLUtils.toSQLString(stmtList, dbType, null, null, tableMapping);
    }

    public static boolean containsIndexDDL(String sql, String dbType) {
        List<SQLStatement> stmtList = parseStatements(sql, dbType);

        return false;
    }

    public static long hash(String sql, String dbType) {
        Lexer lexer = SQLParserUtils.createLexer(sql, dbType);

        StringBuilder buf = new StringBuilder(sql.length());

        for (;;) {
            lexer.nextToken();

            Token token = lexer.token();
            if (token == Token.EOF) {
                break;
            }

            if (token == Token.ERROR) {
                return Utils.murmurhash2_64(sql);
            }

            if (buf.length() != 0) {

            }
        }

        return buf.hashCode();
    }
}

