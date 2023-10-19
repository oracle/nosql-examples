package com.example;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Machine {
    MachineId machineId;

    private String name;
    private Date creationDate = new Date();
    private Map<String, String> setting = new HashMap<>();
    private List<Transition> transitions = new ArrayList<>();
    private List<MachineId> childs;

    public MachineId getMachineId() {
        return machineId;
    }

    public void setMachineId(MachineId machineId) {
        this.machineId = machineId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Map<String, String> getSetting() {
        return setting;
    }

    public void setSetting(Map<String, String> setting) {
        this.setting = setting;
    }

    public List<Transition> getTransitions() {
        return transitions;
    }

    public void setTransitions(List<Transition> transitions) {
        this.transitions = transitions;
    }

    public List<MachineId> getChilds() {
        return childs;
    }

    public void setChilds(List<MachineId> childs) {
        this.childs = childs;
    }

    @Override
    public String toString() {
        return "Machine{" +
            "machineId=" + machineId +
            ", name=" + name +
            ", creationDate=" +
                (creationDate != null ? creationDate.toInstant() : null) +
            ", setting=" + setting +
            ", transitions=" + transitions +
            ", childs=" + childs +
            '}';
    }
}
