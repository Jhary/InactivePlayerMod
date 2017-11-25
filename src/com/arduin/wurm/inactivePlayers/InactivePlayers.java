package com.arduin.wurm.inactivePlayers;

import com.wurmonline.server.DbConnector;
import org.gotti.wurmunlimited.modloader.interfaces.Configurable;
import org.gotti.wurmunlimited.modloader.interfaces.ServerStartedListener;
import org.gotti.wurmunlimited.modloader.interfaces.WurmServerMod;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InactivePlayers implements WurmServerMod, ServerStartedListener, Configurable {

    private Logger logger = Logger.getLogger("InactivePlayersMod");
    private final String fileName = "inactivePlayers.txt";

    private boolean isActive=false;
    private int inactiveDaysThreshold = 90;

    private final String getInactivePlayers =
            "SELECT " +
                    "NAME," +
                    "(PLAYERS.PLAYINGTIME/1000/60/60/24) as daysplayed," +
                    "(CAST((julianday('now') - 2440587.5)*86400000 AS INTEGER) - LASTLOGOUT)/1000/60/60/24 as daysoffline " +
            "FROM PLAYERS WHERE " +
                    "(CAST((julianday('now') - 2440587.5)*86400000 AS INTEGER) - LASTLOGOUT)/1000/60/60/24 > ?"+
            " GROUP BY daysoffline;";

    private final String getDeeds = "SELECT NAME FROM VILLAGES WHERE MAYOR == ?;";

    @Override
    public void onServerStarted() {
        File oldFile = new File(fileName);
        oldFile.delete();
        if(this.isActive){
            this.compileData();
        }

    }

    private void compileData(){
        logger.log(Level.INFO, "Compiling list of players with a minimum of "+ this.inactiveDaysThreshold + " days of inactivity..");
        long timeStart = System.currentTimeMillis();
        try {
            final Connection playerDB = DbConnector.getPlayerDbCon();
            final Connection zonesDB = DbConnector.getZonesDbCon();

            final PreparedStatement psGetPlayers = playerDB.prepareStatement(getInactivePlayers);
            psGetPlayers.setInt(1, this.inactiveDaysThreshold);

            final ResultSet rsGetPlayers = psGetPlayers.executeQuery();

            final PreparedStatement psGetDeeds = zonesDB.prepareStatement(getDeeds);

            PrintWriter writer = new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(fileName)), "UTF-8"));

            writer.format("%-20s\t%-20s\t%-20s\t%-20s\n", "playername", "total days played", "total days offline", "deedname");
            while (rsGetPlayers.next()) {

                psGetDeeds.setString(1, rsGetPlayers.getString("NAME"));
                ResultSet rsGetDeeds = psGetDeeds.executeQuery();
                String formattedLine ="";
                if(rsGetDeeds.next()){
                    formattedLine = String.format("%-20s\t%-20s\t%-20s\t%-20s\n",
                            rsGetPlayers.getString("NAME"),
                            rsGetPlayers.getString("daysplayed"),
                            rsGetPlayers.getString("daysoffline"),
                            rsGetDeeds.getString("NAME"));
                }else{
                    formattedLine = String.format("%-20s\t%-20s\t%-20s\t%-20s\n",
                            rsGetPlayers.getString("NAME"),
                            rsGetPlayers.getString("daysplayed"),
                            rsGetPlayers.getString("daysoffline"),
                            "-");
                }

                writer.append(formattedLine);
                rsGetDeeds.close();
            }
            writer.close();
            rsGetPlayers.close();

            playerDB.close();
            zonesDB.close();

            long timeEnd = System.currentTimeMillis();
            logger.log(Level.INFO, "Finished after "+ (timeEnd-timeStart) +"ms.");
            logger.log(Level.INFO, "Open the "+this.fileName+" file in the server root folder to view the results.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getVersion() {
        return "v1.0";
    }

    @Override
    public void configure(Properties properties) {
        this.isActive = Boolean.parseBoolean(properties.getProperty("active", Boolean.toString(this.isActive)));
        this.inactiveDaysThreshold = Integer.parseInt(properties.getProperty("inactiveDays", Integer.toString(this.inactiveDaysThreshold)));
    }
}
