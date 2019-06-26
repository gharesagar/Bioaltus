package model;

public class FetchEmp {

    private int empId;
    private String empName;

    public FetchEmp(int empId, String empName) {
        this.empId = empId;
        this.empName = empName;
    }

    public int getEmpId() {
        return empId;
    }

    public String getEmpName() {
        return empName;
    }
}
