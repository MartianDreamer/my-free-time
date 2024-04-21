package me.martiandreamer.control;

import jakarta.enterprise.context.ApplicationScoped;
import me.martiandreamer.adapter.MyTimeAdapter;
import me.martiandreamer.model.CheckStatus;
import me.martiandreamer.model.EmployeeInfo;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.List;

@ApplicationScoped
public class CommunicationService {

    private final MyTimeAdapter myTimeAdapter;
    private EmployeeInfo employeeInfo;

    public CommunicationService(@RestClient MyTimeAdapter myTimeAdapter) {
        this.myTimeAdapter = myTimeAdapter;
    }


    public void check() {
        try {
            EmployeeInfo employeeInfo = getEmployeeInfo();
            myTimeAdapter.checkInOut(employeeInfo.id(), employeeInfo.token());
        } catch (Exception e) {
            refreshEmployeeInfo();
            EmployeeInfo employeeInfo = getEmployeeInfo();
            myTimeAdapter.checkInOut(employeeInfo.id(), employeeInfo.token());
        }
    }


    public EmployeeInfo getEmployeeInfo() {
        if (employeeInfo != null) {
            return employeeInfo;
        }
        refreshEmployeeInfo();
        return employeeInfo;
    }

    public CheckStatus checkStatus() {
        return myTimeAdapter.getCurrentStatus(getEmployeeInfo().id());
    }

    @SuppressWarnings("ReassignedVariable")
    public String getWindowsAccount() {
        String username = System.getProperty("user.name");
        if (username == null) {
            username = System.getProperty("user_name");
        }
        return username;
    }

    private void refreshEmployeeInfo() {
        String user = getWindowsAccount();
        List<String> response = myTimeAdapter.getAccessTokenOfAnEmployee(user);
        if (response.size() < 2) {
            throw new RuntimeException("unexpected response");
        }
        employeeInfo = new EmployeeInfo(response.get(0), response.get(1));
    }
}