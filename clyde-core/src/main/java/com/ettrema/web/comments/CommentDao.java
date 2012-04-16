package com.ettrema.web.comments;

import com.ettrema.db.Table;
import com.ettrema.db.TableDefinitionSource;
import com.ettrema.vfs.PostgresUtils;
import com.ettrema.web.BaseResource;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author brad
 */
public class CommentDao implements TableDefinitionSource {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CommentDao.class);
    public final static CommentTable TABLE = new CommentTable();

    public void insert(Comment newComment) {
        log.trace("insert");
        UUID hostId = newComment.page().getHost().getNameNodeId();
        UUID nameId = newComment.getNameNode().getId();
        Date datePosted = newComment.getDate();
        String pagePath = newComment.page().getUrl();

        String sql = TABLE.getInsert();
        try {
            PreparedStatement stmt = PostgresUtils.con().prepareStatement(sql);
            TABLE.hostId.set(stmt, 1, hostId.toString());
            TABLE.nameId.set(stmt, 2, nameId.toString());
            TABLE.datePosted.set(stmt, 3, new Timestamp(datePosted.getTime()));
            TABLE.pagePath.set(stmt, 4, pagePath);

            stmt.execute();
        } catch (SQLException ex) {
            throw new RuntimeException("nameId:" + nameId + " - " + sql, ex);
        }
    }

    public void search(CommentCollector collector, BaseResource forResource) {
        UUID hostId = forResource.getHost().getNameNodeId();
        String path = forResource.getUrl();
        String sql = TABLE.getSelect() + " WHERE " + TABLE.hostId.getName() + " = ? ";
        sql += " AND " + TABLE.pagePath.getName() + " LIKE  ? || '%'";
        sql += " ORDER BY " + TABLE.datePosted.getName() + " DESC";
        PreparedStatement stmt = null;
        try {
            stmt = PostgresUtils.con().prepareStatement(sql);
            stmt.setString(1, hostId.toString());
            stmt.setString(2, path);

            ResultSet rs = null;
            try {
                long tm = System.currentTimeMillis();
                rs = stmt.executeQuery();
                if (log.isTraceEnabled()) {
                    tm = System.currentTimeMillis() - tm;
                    log.trace("executed media sql in: " + tm + "ms");
                }
                int num = 0;
                while (rs.next()) {
                    num++;
                    UUID nameId = UUID.fromString(rs.getString(2));
                    Date datePosted = rs.getTimestamp(3);
                    String pagePath = rs.getString(4);
                    boolean keepGoing = collector.onResult(nameId, datePosted, pagePath);
                    if (!keepGoing) {
                        break;
                    }
                }
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            } finally {
                PostgresUtils.close(rs);
            }
        } catch (SQLException ex) {
            throw new RuntimeException(sql, ex);
        } finally {
            PostgresUtils.close(stmt);
        }
    }

    @Override
    public List<? extends Table> getTableDefinitions() {
        List<Table> list = new ArrayList<>();
        list.add(TABLE);
        return list;
    }

    @Override
    public void onCreate(Table table, Connection cnctn) {
        
    }
}
