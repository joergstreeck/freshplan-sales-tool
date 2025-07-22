# FC-017: Sales Gamification - Technisches Konzept

**Status:** 📋 PLANNED  
**Claude Tech:** [FC-017_CLAUDE_TECH.md](/docs/features/PLANNED/99_sales_gamification/FC-017_CLAUDE_TECH.md)

## Navigation
- **Zurück:** [FC-016 Opportunity Cloning](/docs/features/PLANNED/18_opportunity_cloning/FC-016_TECH_CONCEPT.md)
- **Weiter:** [FC-018 Mobile PWA](/docs/features/PLANNED/09_mobile_app/FC-018_TECH_CONCEPT.md)
- **Übersicht:** [Master Plan V5](/docs/CRM_COMPLETE_MASTER_PLAN_V5.md)

## Übersicht
Gamification-System zur Steigerung der Sales-Team-Motivation durch Punkte, Achievements, Leaderboards und Team-Challenges. Wissenschaftlich fundiertes System zur Leistungssteigerung.

## Feature-Beschreibung

### Kernfunktionalitäten
1. **Point System**
   - Deal-basierte Punkte (Größe & Komplexität)
   - Activity-Punkte (Calls, Meetings, E-Mails)
   - Bonus-Punkte (Streak, First Deal, Team-Assist)
   - Multiplier Events (Double Point Days)

2. **Achievement System**
   - 20+ Badges in 4 Kategorien
   - Rarity Levels (Common → Legendary)
   - Progressive Achievements
   - Secret Achievements

3. **Leaderboards**
   - Real-time Rankings
   - Multiple Zeiträume (Daily, Weekly, Monthly, All-Time)
   - Team vs Individual
   - Department Competitions

4. **Team Challenges**
   - Monthly Team Goals
   - Collaborative Achievements
   - Real-world Rewards
   - Progress Visualization

5. **Reward System**
   - Virtual Rewards (Badges, Titles)
   - Real Rewards (Team Events, Goodies)
   - Recognition Wall
   - Champion Ceremony

## Technische Architektur

### Frontend-Komponenten
```
components/
├── gamification/
│   ├── PointsDisplay.tsx
│   ├── LeaderboardWidget.tsx
│   ├── AchievementGrid.tsx
│   ├── ProgressBar.tsx
│   ├── StreakCounter.tsx
│   └── ChampionCelebration.tsx
├── widgets/
│   ├── DashboardGamificationCard.tsx
│   └── ProfileBadgeShowcase.tsx
└── providers/
    └── GamificationProvider.tsx
```

### Backend-Services
```
services/
├── GamificationService
│   ├── PointCalculator
│   ├── AchievementEngine
│   ├── LeaderboardManager
│   └── RewardDistributor
├── events/
│   ├── PointEventProcessor
│   └── AchievementUnlockHandler
└── scheduled/
    ├── DailyStreakChecker
    └── MonthlyChampionJob
```

### Datenmodell
```sql
-- User Statistics
CREATE TABLE user_stats (
    id UUID PRIMARY KEY,
    user_id UUID REFERENCES users(id),
    total_points INTEGER DEFAULT 0,
    current_month_points INTEGER DEFAULT 0,
    current_streak INTEGER DEFAULT 0,
    longest_streak INTEGER DEFAULT 0,
    last_activity_date DATE,
    level INTEGER DEFAULT 1,
    experience INTEGER DEFAULT 0,
    created_at TIMESTAMP
);

-- Point Transactions
CREATE TABLE point_transactions (
    id UUID PRIMARY KEY,
    user_id UUID REFERENCES users(id),
    points INTEGER NOT NULL,
    transaction_type VARCHAR(50), -- 'deal_closed', 'activity', 'bonus'
    entity_type VARCHAR(50),
    entity_id UUID,
    multiplier DECIMAL(3,2) DEFAULT 1.0,
    description TEXT,
    created_at TIMESTAMP
);

-- Achievements
CREATE TABLE achievements (
    id UUID PRIMARY KEY,
    code VARCHAR(50) UNIQUE,
    name VARCHAR(100),
    description TEXT,
    category VARCHAR(50),
    rarity VARCHAR(20), -- 'common', 'rare', 'epic', 'legendary'
    icon_url TEXT,
    points INTEGER,
    criteria JSONB,
    is_secret BOOLEAN DEFAULT FALSE
);

-- User Achievements
CREATE TABLE user_achievements (
    user_id UUID REFERENCES users(id),
    achievement_id UUID REFERENCES achievements(id),
    unlocked_at TIMESTAMP,
    progress INTEGER DEFAULT 0,
    PRIMARY KEY (user_id, achievement_id)
);

-- Team Challenges
CREATE TABLE team_challenges (
    id UUID PRIMARY KEY,
    name VARCHAR(200),
    description TEXT,
    target_value INTEGER,
    current_value INTEGER DEFAULT 0,
    reward_description TEXT,
    start_date DATE,
    end_date DATE,
    status VARCHAR(20), -- 'active', 'completed', 'failed'
    department_id UUID
);

-- Leaderboard Snapshots
CREATE TABLE leaderboard_snapshots (
    id UUID PRIMARY KEY,
    period_type VARCHAR(20), -- 'daily', 'weekly', 'monthly'
    period_date DATE,
    rankings JSONB, -- [{user_id, points, rank, change}]
    created_at TIMESTAMP
);
```

## Implementation Details

### Point Calculation Engine
```java
@ApplicationScoped
public class PointCalculator {
    
    @ConfigProperty(name = "gamification.points.deal-multiplier")
    double dealMultiplier = 0.01; // 1 point per 100€
    
    public int calculateDealPoints(Opportunity opportunity) {
        int basePoints = (int)(opportunity.getValue() * dealMultiplier);
        
        // Apply modifiers
        if (opportunity.isFirstDeal()) {
            basePoints *= 2; // Double points for first deal
        }
        
        if (opportunity.getDaysToClose() < 30) {
            basePoints *= 1.2; // 20% bonus for quick close
        }
        
        // Size bonuses
        if (opportunity.getValue() > 50000) {
            basePoints += 500; // Big deal bonus
        }
        
        return basePoints;
    }
    
    public int calculateActivityPoints(Activity activity) {
        return switch (activity.getType()) {
            case CALL -> 10;
            case MEETING -> 25;
            case EMAIL -> 5;
            case PROPOSAL_SENT -> 50;
            default -> 1;
        };
    }
}
```

### Achievement Engine
```java
@ApplicationScoped
public class AchievementEngine {
    
    @Inject
    Event<AchievementUnlockedEvent> achievementEvent;
    
    public void checkAchievements(UUID userId, String trigger) {
        List<Achievement> possibleAchievements = 
            achievementRepository.findByTrigger(trigger);
            
        for (Achievement achievement : possibleAchievements) {
            if (evaluateCriteria(userId, achievement)) {
                unlockAchievement(userId, achievement);
            }
        }
    }
    
    private boolean evaluateCriteria(UUID userId, Achievement achievement) {
        JsonObject criteria = achievement.getCriteria();
        
        return switch (criteria.getString("type")) {
            case "deal_count" -> checkDealCount(userId, criteria);
            case "total_points" -> checkTotalPoints(userId, criteria);
            case "streak_days" -> checkStreak(userId, criteria);
            case "team_assist" -> checkTeamAssist(userId, criteria);
            default -> false;
        };
    }
    
    private void unlockAchievement(UUID userId, Achievement achievement) {
        // Save to database
        userAchievementRepository.unlock(userId, achievement.getId());
        
        // Fire event for notifications
        achievementEvent.fire(new AchievementUnlockedEvent(
            userId, achievement
        ));
        
        // Award points
        awardPoints(userId, achievement.getPoints(), "achievement");
    }
}
```

### Leaderboard Manager
```java
@ApplicationScoped
public class LeaderboardManager {
    
    @Scheduled(every = "5m")
    void updateLeaderboards() {
        updateDailyLeaderboard();
        updateWeeklyLeaderboard();
        updateMonthlyLeaderboard();
    }
    
    public LeaderboardDTO getLeaderboard(
        String period, 
        UUID departmentId,
        int limit
    ) {
        List<UserRanking> rankings = userStatsRepository
            .getTopUsers(period, departmentId, limit);
            
        // Calculate position changes
        for (UserRanking ranking : rankings) {
            ranking.setChange(
                calculatePositionChange(ranking, period)
            );
        }
        
        return LeaderboardDTO.builder()
            .period(period)
            .rankings(rankings)
            .lastUpdated(Instant.now())
            .build();
    }
}
```

### Real-time Updates
```typescript
// Frontend WebSocket Integration
export const GamificationProvider: React.FC = ({ children }) => {
  const [points, setPoints] = useState(0);
  const [notifications, setNotifications] = useState<Notification[]>([]);
  
  useEffect(() => {
    const ws = new WebSocket('ws://localhost:8080/gamification');
    
    ws.onmessage = (event) => {
      const data = JSON.parse(event.data);
      
      switch (data.type) {
        case 'POINTS_AWARDED':
          setPoints(prev => prev + data.points);
          showPointAnimation(data.points);
          break;
          
        case 'ACHIEVEMENT_UNLOCKED':
          showAchievementNotification(data.achievement);
          break;
          
        case 'LEADERBOARD_UPDATE':
          updateLeaderboard(data.rankings);
          break;
      }
    };
    
    return () => ws.close();
  }, []);
  
  return (
    <GamificationContext.Provider value={{ points, notifications }}>
      {children}
    </GamificationContext.Provider>
  );
};
```

## API Endpoints

```java
// Gamification API
GET    /api/gamification/stats/:userId
GET    /api/gamification/leaderboard
GET    /api/gamification/achievements
GET    /api/gamification/achievements/:userId
POST   /api/gamification/claim-reward/:achievementId
GET    /api/gamification/challenges
GET    /api/gamification/challenges/:challengeId/progress

// Admin API
POST   /api/admin/gamification/points/award
POST   /api/admin/gamification/achievements
PUT    /api/admin/gamification/challenges/:id
POST   /api/admin/gamification/events
```

## Security Considerations

1. **Point Manipulation**:
   - Alle Punkt-Vergaben über Backend
   - Audit Log für alle Transaktionen
   - Admin-Only Manual Awards

2. **Achievement Cheating**:
   - Server-side Validation
   - Rate Limiting für Activities
   - Duplicate Detection

3. **Privacy**:
   - Opt-out Möglichkeit
   - Anonyme Leaderboards Option
   - DSGVO-konforme Datennutzung

## Performance Optimierung

1. **Caching**:
   - Leaderboard Cache (5 Minuten)
   - User Stats Cache (1 Minute)
   - Achievement Progress Cache

2. **Database Optimization**:
   - Materialized Views für Rankings
   - Indexes auf häufige Queries
   - Partitionierung für point_transactions

3. **Real-time Updates**:
   - WebSocket für Live-Updates
   - Batching von Notifications
   - Progressive Loading

## Feature Flags

```java
@ConfigMapping(prefix = "gamification.features")
public interface GamificationFeatures {
    boolean enabled();
    boolean achievements();
    boolean leaderboards();
    boolean teamChallenges();
    boolean realRewards();
    boolean animations();
}
```

## Testing-Strategie

### Unit Tests
- Point Calculation Logic
- Achievement Criteria Evaluation
- Leaderboard Ranking Algorithm

### Integration Tests
- Event Processing Flow
- WebSocket Updates
- Scheduled Jobs

### E2E Tests
- Complete Gamification Flow
- Achievement Unlock Process
- Leaderboard Updates

## Rollout-Plan

1. **Phase 1**: Basic Points & Display (2 Tage)
2. **Phase 2**: Achievements System (3 Tage)
3. **Phase 3**: Leaderboards (2 Tage)
4. **Phase 4**: Team Challenges (2 Tage)
5. **Phase 5**: Animations & Polish (1 Tag)

## Psychologische Grundlagen

### Motivation Mechanics
1. **Intrinsic Motivation**:
   - Mastery (Level System)
   - Autonomy (Choose Challenges)
   - Purpose (Team Goals)

2. **Extrinsic Motivation**:
   - Recognition (Leaderboards)
   - Rewards (Badges, Events)
   - Competition (Rankings)

### Engagement Loops
1. **Core Loop**: Action → Points → Progress → Reward
2. **Social Loop**: Compare → Compete → Collaborate → Celebrate
3. **Meta Loop**: Daily → Weekly → Monthly → Quarterly Goals

## Metriken & KPIs

1. **Engagement Metrics**:
   - Daily Active Users
   - Average Session Length
   - Feature Adoption Rate

2. **Performance Metrics**:
   - Average Deal Size Change
   - Activity Volume Change
   - Close Rate Improvement

3. **Team Metrics**:
   - Collaboration Score
   - Team Challenge Completion
   - Peer Recognition Count

## Offene Fragen

1. Welche konkreten Team-Rewards?
2. Punishment für Inaktivität (Streak-Verlust)?
3. Seasonale Events (Weihnachts-Challenge)?
4. Integration mit HR-System für echte Boni?
5. Fairness zwischen Junior/Senior Sales?

## Referenzen

- [Octalysis Framework](https://octalysis.com/)
- [Gamification at Work](https://www.gamification.co/)
- Salesforce Trailhead Gamification
- HubSpot Sales Gamification Study

## Verwandte Features

- [FC-027 Magic Moments](/docs/features/PLANNED/27_magic_moments/FC-027_TECH_CONCEPT.md) - Für Celebration-Animationen
- [M4 Opportunity Pipeline](/docs/features/ACTIVE/02_opportunity_pipeline/M4_TECH_CONCEPT.md) - Für Deal-Punkte
- [FC-014 Activity Timeline](/docs/features/PLANNED/16_activity_timeline/FC-014_TECH_CONCEPT.md) - Für Activity-Tracking
- [FC-019 Advanced Metrics](/docs/features/PLANNED/19_advanced_metrics/FC-019_TECH_CONCEPT.md) - Für Performance-KPIs