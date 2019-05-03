package org.servantscode.metrics;

public class MinistyEnrollmentStatistics {
    private int contacts;
    private int leaders;
    private int members;

    public MinistyEnrollmentStatistics() { }

    // ----- Accessors -----
    public int getContacts() { return contacts; }
    public void setContacts(int contacts) { this.contacts = contacts; }
    public void addContacts(int newContacts) { this.contacts += newContacts; }

    public int getLeaders() { return leaders; }
    public void setLeaders(int leaders) { this.leaders = leaders; }
    public void addLeaders(int newLeaders) { this.leaders += newLeaders; }

    public int getMembers() { return members; }
    public void setMembers(int members) { this.members = members; }
    public void addMembers(int newMembers) { this.members += newMembers; }
}
