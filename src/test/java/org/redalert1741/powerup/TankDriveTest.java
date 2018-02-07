package org.redalert1741.powerup;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.redalert1741.robotbase.wrapper.FakeSolenoidWrapper;
import org.redalert1741.robotbase.wrapper.FakeTalonSRXWrapper;
import org.redalert1741.robotbase.wrapper.SolenoidWrapper;
import org.redalert1741.robotbase.wrapper.TalonSRXWrapper;

public class TankDriveTest {
    TankDrive drive;
    TalonSRXWrapper left1, left2, right1, right2;
    SolenoidWrapper shifter;
    
    @Before
    public void initDrive() {
        left1 = new FakeTalonSRXWrapper();
        left2 = new FakeTalonSRXWrapper();
        right1 = new FakeTalonSRXWrapper();
        right2 = new FakeTalonSRXWrapper();
        shifter = new FakeSolenoidWrapper();
        drive = new TankDrive(left1, left2, right1, right2, shifter);
    }
    
    @Test
    public void driveTest() {
        drive.enableDriving();
        assertFalse(shifter.get());
        drive.arcadeDrive(0, 0);
        assertEquals(0, left1.get(), 0.001);
        assertEquals(0, left2.get(), 0.001);
        assertEquals(0, right1.get(), 0.001);
        assertEquals(0, right2.get(), 0.001);
        drive.arcadeDrive(0, 1);
        assertEquals(1, left1.get(), 0.001);
        assertEquals(1, left2.get(), 0.001);
        assertEquals(1, right1.get(), 0.001);
        assertEquals(1, right2.get(), 0.001);
        drive.arcadeDrive(1, 0);
        assertEquals(1, left1.get(), 0.001);
        assertEquals(1, left2.get(), 0.001);
        assertEquals(-1, right1.get(), 0.001);
        assertEquals(-1, right2.get(), 0.001);
    }
}
