package com.tale.bootstrap;

/**
 * @Author lijiangzhou
 * @Date 2019/3/22  10:20
 * @ClassName: MysqlJdbc
 */

import com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

@Slf4j
@NoArgsConstructor
public final class MysqlJdbc {

    private static final Properties jdbc_prop = new Properties();

    public static String JDBC_URL = null;
    public static String JDBC_USER = null;
    public static String JDBC_PASS = null;

    static {

        jdbc_prop.put("initialSize", "5");
        jdbc_prop.put("maxActive", "10");
        jdbc_prop.put("minIdle", "3");
        jdbc_prop.put("maxWait", "60000");
        jdbc_prop.put("removeAbandoned", "true");
        jdbc_prop.put("removeAbandonedTimeout", "180");
        jdbc_prop.put("timeBetweenEvictionRunsMillis", "60000");
        jdbc_prop.put("minEvictableIdleTimeMillis", "300000");
        jdbc_prop.put("validationQuery", "SELECT 1 FROM DUAL");
        jdbc_prop.put("testWhileIdle", "true");
        jdbc_prop.put("testOnBorrow", "false");
        jdbc_prop.put("testOnReturn", "false");
        jdbc_prop.put("poolPreparedStatements", "true");
        jdbc_prop.put("maxPoolPreparedStatementPerConnectionSize", "50");
        jdbc_prop.put("filters", "stat");

        InputStream in = MysqlJdbc.class.getClassLoader().getResourceAsStream("jdbc.properties");
        Properties props = new Properties();
        try {
            props.load(in);
            String driverClassName = props.get("jdbc.driver").toString();
            String username = props.get("jdbc.user").toString();
            String password = props.get("jdbc.pass").toString();
            String url = props.get("jdbc.url").toString();
            put("driverClassName",driverClassName);
            put("url", url);
            put("username", username);
            put("password", password);
            JDBC_URL = url;
            JDBC_USER = username;
            JDBC_PASS = password;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void put(String key, String value) {
        jdbc_prop.remove(key);
        jdbc_prop.put(key, value);
    }

    /**
     * 测试连接并导入数据库
     */
    public static void importSql(boolean devMode) {
        try {

            log.info("blade dev mode: {}", devMode);
            log.info("load mysql database ...");

            //加载MYSQL驱动
            Class.forName("com.mysql.jdbc.Driver");
            Connection con = DriverManager.getConnection(jdbc_prop.getProperty("url"), jdbc_prop.getProperty("username"), jdbc_prop.getProperty("password"));

            Statement statement = con.createStatement();
            ResultSet rs  = null;
            try{
                //测试连接
                rs  = statement.executeQuery("SELECT count(*) FROM t_options");
                log.info("load mysql database successful");
            }catch (MySQLSyntaxErrorException e){
                log.info("load mysql database fail , mysql database not init");
                System.exit(-1);
            }finally {
                if(rs!=null) rs.close();
                statement.close();
                con.close();
            }

        }  catch (Exception e) {
            log.error("initialize database fail", e);
        }
    }

}
