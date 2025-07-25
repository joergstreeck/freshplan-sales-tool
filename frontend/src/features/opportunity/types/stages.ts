// Stage-Definitionen für Opportunity Pipeline
export enum OpportunityStage {
  LEAD = "lead",
  QUALIFIED = "qualified",
  NEEDS_ANALYSIS = "needs_analysis", // Alias für QUALIFIED
  PROPOSAL = "proposal",
  NEGOTIATION = "negotiation",
  CLOSED_WON = "closed_won",
  CLOSED_LOST = "closed_lost"
}

// Stage configuration mit Farben und Labels
export const STAGE_CONFIGS = {
  [OpportunityStage.LEAD]: {
    label: 'Lead',
    color: '#90CAF9',
    probability: 20,
    order: 1,
    canWin: true,
    canLose: true,
  },
  [OpportunityStage.QUALIFIED]: {
    label: 'Qualifiziert',
    color: '#94C456',
    probability: 60,
    order: 2,
    canWin: true,
    canLose: true,
  },
  [OpportunityStage.NEEDS_ANALYSIS]: {
    label: 'Bedarfsanalyse',
    color: '#94C456',
    probability: 60,
    order: 2,
    canWin: true,
    canLose: true,
  },
  [OpportunityStage.PROPOSAL]: {
    label: 'Angebot',
    color: '#FFA726',
    probability: 80,
    order: 3,
    canWin: true,
    canLose: true,
  },
  [OpportunityStage.NEGOTIATION]: {
    label: 'Verhandlung',
    color: '#FF7043',
    probability: 90,
    order: 4,
    canWin: true,
    canLose: true,
  },
  [OpportunityStage.CLOSED_WON]: {
    label: 'Gewonnen',
    color: '#66BB6A',
    probability: 100,
    order: 5,
    canWin: false,
    canLose: false,
    canReactivate: true,
    isWon: true,
  },
  [OpportunityStage.CLOSED_LOST]: {
    label: 'Verloren',
    color: '#EF5350',
    probability: 0,
    order: 6,
    canWin: false,
    canLose: false,
    canReactivate: true,
    isLost: true,
  },
};