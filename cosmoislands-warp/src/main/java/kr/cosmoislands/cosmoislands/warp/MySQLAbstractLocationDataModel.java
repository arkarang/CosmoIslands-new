package kr.cosmoislands.cosmoislands.warp;

import kr.cosmoislands.cosmoislands.api.AbstractLocation;
import kr.cosmoislands.cosmoislands.core.Database;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;

@RequiredArgsConstructor
public class MySQLAbstractLocationDataModel {

    protected final Database database;
    protected final String table;

    @Nullable
    protected AbstractLocation getLocation(ResultSet rs) throws SQLException {
            double x, y, z;
            float yaw, pitch;
            x = rs.getDouble(1);
            y = rs.getDouble(2);
            z = rs.getDouble(3);
            yaw = rs.getFloat(4);
            pitch = rs.getFloat(5);
            return new AbstractLocation(x, y, z, yaw, pitch);
    }

}
