package org.redalert1741.powerup;

import edu.wpi.first.wpilibj.GenericHID.Hand;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.XboxController;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.redalert1741.powerup.auto.end.TalonDistanceEnd;
import org.redalert1741.powerup.auto.move.TankDriveArcadeMove;
import org.redalert1741.powerup.auto.move.TankDriveBrakeMove;
import org.redalert1741.robotbase.auto.core.AutoFactory;
import org.redalert1741.robotbase.auto.core.Autonomous;
import org.redalert1741.robotbase.auto.core.JsonAutoFactory;
import org.redalert1741.robotbase.auto.end.EmptyEnd;
import org.redalert1741.robotbase.config.Config;
import org.redalert1741.robotbase.logging.DataLogger;
import org.redalert1741.robotbase.logging.LoggablePdp;
import org.redalert1741.robotbase.wrapper.RealDoubleSolenoidWrapper;
import org.redalert1741.robotbase.wrapper.RealSolenoidWrapper;
import org.redalert1741.robotbase.wrapper.RealTalonSrxWrapper;
import org.redalert1741.robotbase.wrapper.TalonSrxWrapper;

public class Robot extends IterativeRobot {
    private DataLogger data;
    private Config config;
    private Autonomous auto;

    private RealDoubleSolenoidWrapper tilt;
    private RealDoubleSolenoidWrapper grab;
    private RealDoubleSolenoidWrapper kick;
    private RealSolenoidWrapper driveBrake;
    private RealSolenoidWrapper manipBrake;

    private TankDrive drive;
    private Manipulation manip;
    private Scoring score;
    private LoggablePdp pdp;

    private XboxController driver;
    private XboxController operator;

    private long enableStart;

    @Override
    public void robotInit() {
        driver = new XboxController(0);
        operator = new XboxController(1);

        pdp = new LoggablePdp();

        TalonSrxWrapper rightDrive = new RealTalonSrxWrapper(2);

        setupSolenoids();

        drive = new TankDrive(new RealTalonSrxWrapper(4), new RealTalonSrxWrapper(5),
                rightDrive, new RealTalonSrxWrapper(3),
                driveBrake);
        manip = new Manipulation(new RealTalonSrxWrapper(1), new RealTalonSrxWrapper(7),
                new RealTalonSrxWrapper(6),
                tilt, manipBrake);
        score = new Scoring(kick, grab);

        //logging

        data = new DataLogger();

        data.addAttribute("time");

        data.addLoggable(pdp);
        data.addLoggable(drive);
        data.addLoggable(manip);
        data.addLoggable(score);

        data.setupLoggables();

        //config

        config = new Config();

        config.addConfigurable(drive);
        config.addConfigurable(manip);

        reloadConfig();

        //auto moves

        AutoFactory.addMoveMove("drive", () -> new TankDriveArcadeMove(drive));
        AutoFactory.addMoveMove("drivebrake", () -> new TankDriveBrakeMove(drive));
        AutoFactory.addMoveEnd("driveDist", () -> new TalonDistanceEnd(rightDrive));
        AutoFactory.addMoveEnd("empty", () -> new EmptyEnd());
    }

    @Override
    public void autonomousInit() {
        startLogging(data, "auto");
        reloadConfig();

        score.close();
        score.retract();
        drive.setBrakes(false);

        auto = new JsonAutoFactory().makeAuto("/home/lvuser/auto/min-auto.json");
        auto.start();

        enableStart = System.currentTimeMillis();
    }

    @Override
    public void autonomousPeriodic() {
        auto.run();
        data.log("time", System.currentTimeMillis()-enableStart);
        data.logAll();
    }

    @Override
    public void teleopInit() {
        startLogging(data, "teleop");
        reloadConfig();

        score.grabOff();
        drive.setBrakes(false);
    }

    @Override
    public void teleopPeriodic() {
        //driving
        drive.arcadeDrive(driver.getX(Hand.kRight)*0.5, -0.5*driver.getY(Hand.kLeft));

        //manual manipulation controls
        manip.setFirstStage(-operator.getY(Hand.kLeft));
        manip.setSecondStage(-operator.getY(Hand.kRight));
//        if(driver.getStartButton()) {
//            manip.setLift(driver.getTriggerAxis(Hand.kRight));
//            manip.setSecond(driver.getTriggerAxis(Hand.kLeft));
//        }

        //tilt manipulation
        if(driver.getAButton()) {
            manip.tiltOut();
        }
        if(driver.getBButton()) {
            manip.tiltIn();
        }

        //scoring controls
        if(driver.getYButton()) {
            score.kick();
        } else {
            score.retract();
        }
        if(driver.getBumper(Hand.kLeft)) {
            score.close();
        }
        if(driver.getBumper(Hand.kRight)) {
            score.open();
        }
        if(driver.getXButton()) {
            score.grabOff();
        }

        //reset manipulation
        if(operator.getBackButton()) {
            manip.resetPosition();
        }

        //climbing controls (just drive backwards to climb)
        if(driver.getPOV() == 90) {
            drive.enableClimbing();
        } else if(driver.getPOV() == 270) {
            drive.enableDriving();
        }

        if(operator.getPOV() == 180) {
            manip.setFirstStagePosition(0);
        } else if(operator.getPOV() == 90) {
            manip.setFirstStagePosition(-4100);
        }

        data.log("time", System.currentTimeMillis()-enableStart);
        data.logAll();
    }

    @Override
    public void testPeriodic() {
        // TODO: Add code to be called in test mode
    }

    private void startLogging(DataLogger data, String type) {
        data.open("/home/lvuser/logs/log"
                +new SimpleDateFormat("-yyyy-MM-dd_HH-mm-ss-").format(new Date())
                +type+".csv");
        data.writeAttributes();

        enableStart = System.currentTimeMillis();
    }

    private void reloadConfig() {
        config.loadFromFile("/home/lvuser/config.txt");
        config.reloadConfig();
    }

    private void setupSolenoids() {
        Config solenoidconfig = new Config();
        solenoidconfig.loadFromFile("/home/lvuser/solenoids.txt");
        String solenoids = solenoidconfig.getSetting("solenoids", "62743051");
        driveBrake = new RealSolenoidWrapper(solenoids.charAt(0)-'0');
        tilt = new RealDoubleSolenoidWrapper(solenoids.charAt(1)-'0', solenoids.charAt(2)-'0');
        manipBrake = new RealSolenoidWrapper(solenoids.charAt(3)-'0');
        kick = new RealDoubleSolenoidWrapper(solenoids.charAt(4)-'0', solenoids.charAt(5)-'0');
        grab = new RealDoubleSolenoidWrapper(solenoids.charAt(6)-'0', solenoids.charAt(7)-'0');
    }
}
