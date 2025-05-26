package com.ssafy.util;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.Statement;

@Component
@RequiredArgsConstructor
public class SqlDumpRunner implements CommandLineRunner {

    private final DataSource dataSource;

    @Override
    public void run(String... args) throws Exception {
        // -- 배포 환경 대응 로그
        System.out.println("[⚙] Initializing SQL schema and data dump...");

        // 테이블 생성용 스키마 SQL
        String[] schemaFiles = {
                "aptdb_user.sql",
                "aptdb_sido_code.sql",
                "aptdb_gugun_code.sql",
                "aptdb_dong_code.sql",
                "aptdb_house_infos.sql",
                "aptdb_house_deals.sql",
                "aptdb_recent_deals.sql",
                "aptdb_review.sql",
                "aptdb_board.sql",
                "aptdb_file_info.sql",
                "aptdb_interest_area.sql",
                "aptdb_interest_house.sql",
                "aptdb_oauth_entity.sql",
                "aptdb_chat_history.sql",
                "aptdb_verification_code.sql"
        };

        // 데이터만 포함된 덤프 SQL
        String[] dataFiles = schemaFiles; // 파일 이름 동일하므로 재사용

        try (Connection conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("SET NAMES utf8mb4");
            stmt.execute("SET FOREIGN_KEY_CHECKS = 0");

            for (String file : schemaFiles) {
                String path = "init/" + file;
                System.out.println("[📄] Creating schema from: " + path);
                executeSql(conn, path);
            }

            for (String file : dataFiles) {
                try {
                    System.out.println("[📦] Loading data from: dump/" + file);
                    executeDataOnlySql(conn, file);
                    System.out.println("[✔] Loaded SQL data: " + file);
                } catch (Exception e) {
                    System.err.println("[✘] Failed to load: " + file);
                    e.printStackTrace();
                    throw e;
                }
            }

            stmt.execute("SET FOREIGN_KEY_CHECKS = 1");
            System.out.println("[✅] Database initialization completed.");
        }
    }

    private void executeSql(Connection conn, String path) throws Exception {
        var resource = new ClassPathResource(path);
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()));
                Statement stmt = conn.createStatement()
        ) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("--") || line.isEmpty()) continue;
                sb.append(line).append(" ");
                if (line.endsWith(";")) {
                    stmt.execute(sb.toString());
                    sb.setLength(0);
                }
            }
        }
    }

    private void executeDataOnlySql(Connection conn, String filename) throws Exception {
        var resource = new ClassPathResource("dump/" + filename);
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()));
                Statement stmt = conn.createStatement()
        ) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("--") || line.isEmpty()) continue;

                // CREATE/DROP/ALTER 등 DDL 명령 무시
                String upper = line.toUpperCase();
                if (upper.startsWith("CREATE") || upper.startsWith("DROP") || upper.startsWith("ALTER")) continue;

                // 문자셋 호환 처리
                line = line.replace("utf8mb3", "utf8mb4").replace("utf8mb3_bin", "utf8mb4_bin");

                sb.append(line).append(" ");
                if (line.endsWith(";")) {
                    stmt.execute(sb.toString());
                    sb.setLength(0);
                }
            }
        }
    }
}
