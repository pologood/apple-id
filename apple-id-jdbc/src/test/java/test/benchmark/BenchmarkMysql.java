package test.benchmark;

import java.sql.SQLException;

import org.apache.commons.dbcp2.BasicDataSource;

import com.appleframework.id.JdbcIdGenerator;
import com.mysql.jdbc.AbandonedConnectionCleanupThread;

import test.utils.StringUtils;

public class BenchmarkMysql extends BaseBenchmarkSerialId {

    public static void main(String[] args) throws SQLException, InterruptedException {
        int numRuns, numThreads, numNamespaces;

        try {
            numRuns = Integer.parseInt(System.getProperty("numRuns"));
        } catch (Exception e) {
            numRuns = 32000;
        }
        try {
            numThreads = Integer.parseInt(System.getProperty("numThreads"));
        } catch (Exception e) {
            numThreads = 4;
        }
        try {
            numNamespaces = Integer.parseInt(System.getProperty("numNamespaces"));
        } catch (Exception e) {
            numNamespaces = 4;
        }

        String jdbcUrl = System.getProperty("jdbcUrl");
        String jdbcUser = System.getProperty("jdbcUser");
        String jdbcPassword = System.getProperty("jdbcPassword");
        
        if (StringUtils.isBlank(jdbcUrl)) {
            jdbcUrl = "jdbc:mysql://localhost:3306/temp";
        }
        if (StringUtils.isBlank(jdbcUser)) {
            jdbcUser = "test";
        }
        if (StringUtils.isBlank(jdbcPassword)) {
            jdbcPassword = "test";
        }

        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName("com.mysql.jdbc.Driver");
        ds.setUrl(jdbcUrl);
        ds.setUsername(jdbcUser);
        ds.setPassword(jdbcPassword);
        ds.setInitialSize(2);
        ds.setMaxTotal(4);
        ds.setPoolPreparedStatements(false);
        ds.setTestOnBorrow(true);
        ds.setValidationQuery("SELECT 1");
        ds.setValidationQueryTimeout(1);
        final JdbcIdGenerator idGenerator = new JdbcIdGenerator();
        idGenerator.setTableName("id_server").setDataSource(ds).init();

        initValues(idGenerator, numNamespaces);

        for (int i = 0; i < 10; i++) {
            runTest(idGenerator, numRuns, numThreads, numNamespaces, "MySQL");
        }

        printValues(idGenerator, numNamespaces);

        idGenerator.destroy();
        ds.close();
        AbandonedConnectionCleanupThread.shutdown();
    }
}
