package com.freshfoodz.crm.help.operations;

import java.util.List;
import java.util.Map;

/**
 * CAR (Calibrated Assistive Rollout) Response for Operations-Guidance
 * Strukturierte Response für Guided Operations im Help-System
 */
public class CARResponse {
    private String title;
    private String quickSummary;
    private String businessContext;
    private List<String> actionSteps;
    private Map<String, String> troubleshooting;
    private String escalation;
    private List<String> nextSteps;
    private List<String> kpis;
    private List<String> businessRules;
    private List<String> monitoring;
    private List<String> validation;
    private List<String> relatedDocumentation;

    // Private constructor for builder pattern
    private CARResponse() {}

    public static GuidedOperationBuilder guidedOperation() {
        return new GuidedOperationBuilder();
    }

    // Getters
    public String getTitle() { return title; }
    public String getQuickSummary() { return quickSummary; }
    public String getBusinessContext() { return businessContext; }
    public List<String> getActionSteps() { return actionSteps; }
    public Map<String, String> getTroubleshooting() { return troubleshooting; }
    public String getEscalation() { return escalation; }
    public List<String> getNextSteps() { return nextSteps; }
    public List<String> getKpis() { return kpis; }
    public List<String> getBusinessRules() { return businessRules; }
    public List<String> getMonitoring() { return monitoring; }
    public List<String> getValidation() { return validation; }
    public List<String> getRelatedDocumentation() { return relatedDocumentation; }

    public static class GuidedOperationBuilder {
        private final CARResponse response = new CARResponse();

        public GuidedOperationBuilder title(String title) {
            response.title = title;
            return this;
        }

        public GuidedOperationBuilder quickSummary(String summary) {
            response.quickSummary = summary;
            return this;
        }

        public GuidedOperationBuilder businessContext(String context) {
            response.businessContext = context;
            return this;
        }

        public GuidedOperationBuilder actionSteps(List<String> steps) {
            response.actionSteps = steps;
            return this;
        }

        public GuidedOperationBuilder troubleshooting(Map<String, String> troubleshooting) {
            response.troubleshooting = troubleshooting;
            return this;
        }

        public GuidedOperationBuilder escalation(String escalation) {
            response.escalation = escalation;
            return this;
        }

        public GuidedOperationBuilder nextSteps(List<String> steps) {
            response.nextSteps = steps;
            return this;
        }

        public GuidedOperationBuilder kpis(List<String> kpis) {
            response.kpis = kpis;
            return this;
        }

        public GuidedOperationBuilder businessRules(List<String> rules) {
            response.businessRules = rules;
            return this;
        }

        public GuidedOperationBuilder monitoring(List<String> monitoring) {
            response.monitoring = monitoring;
            return this;
        }

        public GuidedOperationBuilder validation(List<String> validation) {
            response.validation = validation;
            return this;
        }

        public GuidedOperationBuilder relatedDocumentation(List<String> docs) {
            response.relatedDocumentation = docs;
            return this;
        }

        public CARResponse build() {
            return response;
        }
    }
}