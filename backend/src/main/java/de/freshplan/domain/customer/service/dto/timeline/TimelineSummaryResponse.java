package de.freshplan.domain.customer.service.dto.timeline;

/**
 * Response DTO for timeline summary statistics.
 * 
 * @author FreshPlan Team
 * @since 2.0.0
 */
public class TimelineSummaryResponse {
    
    private long totalEvents;
    private long communicationEvents;
    private long meetingEvents;
    private long taskEvents;
    private long systemEvents;
    
    // Constructors
    public TimelineSummaryResponse() {}
    
    public TimelineSummaryResponse(
            long totalEvents,
            long communicationEvents,
            long meetingEvents,
            long taskEvents,
            long systemEvents) {
        this.totalEvents = totalEvents;
        this.communicationEvents = communicationEvents;
        this.meetingEvents = meetingEvents;
        this.taskEvents = taskEvents;
        this.systemEvents = systemEvents;
    }
    
    // Getters and Setters
    public long getTotalEvents() {
        return totalEvents;
    }
    
    public void setTotalEvents(long totalEvents) {
        this.totalEvents = totalEvents;
    }
    
    public long getCommunicationEvents() {
        return communicationEvents;
    }
    
    public void setCommunicationEvents(long communicationEvents) {
        this.communicationEvents = communicationEvents;
    }
    
    public long getMeetingEvents() {
        return meetingEvents;
    }
    
    public void setMeetingEvents(long meetingEvents) {
        this.meetingEvents = meetingEvents;
    }
    
    public long getTaskEvents() {
        return taskEvents;
    }
    
    public void setTaskEvents(long taskEvents) {
        this.taskEvents = taskEvents;
    }
    
    public long getSystemEvents() {
        return systemEvents;
    }
    
    public void setSystemEvents(long systemEvents) {
        this.systemEvents = systemEvents;
    }
}