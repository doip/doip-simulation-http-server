package doip.simulation.http;

import doip.simulation.api.SimulationManager;

public class SimulationManagerMock implements SimulationManager {

    private boolean startCalled = false;
    private boolean stopCalled = false;
    private String startedPlatform;

    @Override
    public void start(String platform) {
        startCalled = true;
        startedPlatform = platform;
    }

    @Override
    public void stop() {
        stopCalled = true;
    }

    // Additional methods or getters for testing purposes
    public boolean isStartCalled() {
        return startCalled;
    }

    public boolean isStopCalled() {
        return stopCalled;
    }

    public String getStartedPlatform() {
        return startedPlatform;
    }
}
