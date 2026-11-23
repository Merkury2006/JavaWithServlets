package vsu.cs;

import vsu.cs.facade.CarFacade;
import vsu.cs.facade.ClientFacade;
import vsu.cs.facade.WorkOrderFacade;

public final class AppContainer {
    public final ClientFacade clientFacade;
    public final CarFacade carFacade;
    public final WorkOrderFacade workOrderFacade;

    public AppContainer(ClientFacade clientFacade, CarFacade carFacade, WorkOrderFacade workOrderFacade) {
        this.clientFacade = clientFacade;
        this.carFacade = carFacade;
        this.workOrderFacade = workOrderFacade;
    }
}
