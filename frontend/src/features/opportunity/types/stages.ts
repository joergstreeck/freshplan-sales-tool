// Stage-Definitionen aus dem Technical Concept
export enum OpportunityStage {
  LEAD = "lead",
  QUALIFIED = "qualified",
  NEEDS_ANALYSIS = "needs_analysis", // Alias für QUALIFIED
  PROPOSAL = "proposal",
  NEGOTIATION = "negotiation",
  CLOSED_WON = "closed_won",
  CLOSED_LOST = "closed_lost"
}

// Stage-Konfiguration
interface StageConfig {
  stage: OpportunityStage;
  label: string;
  color: string;
  bgColor: string;
}

export const STAGE_CONFIGS: StageConfig[] = [
  { 
    stage: OpportunityStage.LEAD, 
    label: 'Lead', 
    color: '#1976D2', 
    bgColor: '#E3F2FD' 
  },
  { 
    stage: OpportunityStage.QUALIFIED, 
    label: 'Qualifiziert', 
    color: '#388E3C', 
    bgColor: '#E8F5E9' 
  },
  { 
    stage: OpportunityStage.PROPOSAL, 
    label: 'Angebot', 
    color: '#F57C00', 
    bgColor: '#FFF3E0' 
  },
  { 
    stage: OpportunityStage.NEGOTIATION, 
    label: 'Verhandlung', 
    color: '#7B1FA2', 
    bgColor: '#F3E5F5' 
  },
  { 
    stage: OpportunityStage.CLOSED_WON, 
    label: 'Gewonnen', 
    color: '#2E7D32', 
    bgColor: '#C8E6C9' 
  },
  { 
    stage: OpportunityStage.CLOSED_LOST, 
    label: 'Verloren', 
    color: '#D32F2F', 
    bgColor: '#FFCDD2' 
  },
];