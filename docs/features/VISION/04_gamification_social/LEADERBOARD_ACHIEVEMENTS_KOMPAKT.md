# Leaderboards & Achievements - Sales Gamification ⚡

**Feature Code:** V-GAM-001  
**Feature-Typ:** 🎮 GAMIFICATION  
**Geschätzter Aufwand:** 15-18 Tage  
**Priorität:** VISION - Team Motivation  
**ROI:** +25% Activity Level, +15% Team Performance  

---

## 🎯 PROBLEM & LÖSUNG IN 30 SEKUNDEN

**Problem:** Sales ist repetitiv, Motivation sinkt, Team-Spirit fehlt  
**Lösung:** Gamification mit Levels, Badges, Leaderboards  
**Impact:** Spaß bei der Arbeit = Bessere Ergebnisse  

---

## 🏆 GAMIFICATION SYSTEM

```
1. LEADERBOARDS
   Daily | Weekly | Monthly | All-Time
   
2. ACHIEVEMENTS
   "First Deal" | "Call Champion" | "Pipeline Master"
   
3. LEVELS & XP
   Junior (0-1000 XP) → Senior (5000+ XP) → Legend (10k+ XP)
   
4. CHALLENGES
   "5 Calls vor 10 Uhr" | "3 Demos diese Woche"
```

---

## 🏃 IMPLEMENTATION KONZEPT

### Achievement System
```typescript
// Achievement Definition
interface Achievement {
  id: string;
  name: string;
  description: string;
  icon: string;
  category: AchievementCategory;
  rarity: 'common' | 'rare' | 'epic' | 'legendary';
  points: number;
  criteria: AchievementCriteria;
  rewards?: Reward[];
}

// Achievement Engine
export class AchievementEngine {
  private userAchievements = new Map<string, UserAchievement[]>();
  
  async checkAchievements(userId: string, event: ActivityEvent) {
    const eligibleAchievements = await this.getEligibleAchievements(userId);
    
    for (const achievement of eligibleAchievements) {
      if (this.evaluateCriteria(achievement.criteria, event, userId)) {
        await this.awardAchievement(userId, achievement);
      }
    }
  }
  
  private evaluateCriteria(
    criteria: AchievementCriteria, 
    event: ActivityEvent,
    userId: string
  ): boolean {
    switch (criteria.type) {
      case 'count':
        return this.checkCountCriteria(criteria, userId);
      case 'streak':
        return this.checkStreakCriteria(criteria, userId);
      case 'milestone':
        return this.checkMilestoneCriteria(criteria, event);
      case 'special':
        return this.checkSpecialCriteria(criteria, event);
    }
  }
  
  private async awardAchievement(userId: string, achievement: Achievement) {
    // Record achievement
    await this.recordAchievement(userId, achievement);
    
    // Award XP
    await this.awardXP(userId, achievement.points);
    
    // Send notification
    await this.notifyUser(userId, {
      type: 'achievement_unlocked',
      achievement: achievement,
      timestamp: new Date()
    });
    
    // Check for level up
    await this.checkLevelUp(userId);
  }
}
```

### Leaderboard System
```java
@ApplicationScoped
public class LeaderboardService {
    
    @Inject
    Redis redis;
    
    // Real-time leaderboard updates mit Redis Sorted Sets
    public void updateScore(UUID userId, String leaderboardKey, double score) {
        redis.zadd(leaderboardKey, score, userId.toString());
        
        // Update verschiedene Zeiträume
        updateDailyLeaderboard(userId, score);
        updateWeeklyLeaderboard(userId, score);
        updateMonthlyLeaderboard(userId, score);
        updateAllTimeLeaderboard(userId, score);
    }
    
    public List<LeaderboardEntry> getLeaderboard(
        LeaderboardType type, 
        TimePeriod period,
        int limit
    ) {
        String key = buildLeaderboardKey(type, period);
        
        // Get top entries with scores
        return redis.zrevrangeWithScores(key, 0, limit - 1)
            .stream()
            .map(this::mapToLeaderboardEntry)
            .collect(toList());
    }
    
    // Live updates via WebSocket
    @OnOpen
    public void onOpen(Session session) {
        leaderboardSessions.add(session);
    }
    
    void broadcastLeaderboardUpdate(LeaderboardUpdate update) {
        String message = Json.encode(update);
        leaderboardSessions.forEach(session -> {
            session.getAsyncRemote().sendText(message);
        });
    }
}
```

### Frontend Gamification UI
```typescript
// Achievement Popup
export const AchievementUnlockedPopup: React.FC<{
  achievement: Achievement
}> = ({ achievement }) => {
  return (
    <motion.div
      initial={{ scale: 0, opacity: 0 }}
      animate={{ scale: 1, opacity: 1 }}
      exit={{ scale: 0, opacity: 0 }}
      className="achievement-popup"
    >
      <div className="achievement-icon">
        {achievement.icon}
      </div>
      <h3>Achievement Unlocked!</h3>
      <h4>{achievement.name}</h4>
      <p>{achievement.description}</p>
      <div className="achievement-points">
        +{achievement.points} XP
      </div>
    </motion.div>
  );
};

// Leaderboard Component
export const Leaderboard: React.FC = () => {
  const [period, setPeriod] = useState<TimePeriod>('daily');
  const { data: entries } = useLeaderboard(period);
  const currentUser = useCurrentUser();
  
  return (
    <Card>
      <CardHeader
        title="Leaderboard"
        action={
          <ToggleButtonGroup value={period} onChange={(e, v) => setPeriod(v)}>
            <ToggleButton value="daily">Heute</ToggleButton>
            <ToggleButton value="weekly">Woche</ToggleButton>
            <ToggleButton value="monthly">Monat</ToggleButton>
          </ToggleButtonGroup>
        }
      />
      <CardContent>
        <List>
          {entries?.map((entry, index) => (
            <LeaderboardEntry
              key={entry.userId}
              rank={index + 1}
              entry={entry}
              isCurrentUser={entry.userId === currentUser.id}
              showAnimation={index < 3}
            />
          ))}
        </List>
      </CardContent>
    </Card>
  );
};
```

---

## 🔗 GAME MECHANICS

**XP Sources:**
- Calls: 10 XP
- Meetings: 25 XP
- Opportunities: 50 XP
- Deals: 100-500 XP (based on size)
- Daily Login: 5 XP
- Streaks: Bonus XP

**Achievement Categories:**
- Activity (Calls, Meetings)
- Performance (Deals, Revenue)
- Teamwork (Assists, Sharing)
- Special (First of Month, Holiday)

---

## ⚡ ENGAGEMENT FEATURES

1. **Daily Challenges:** Neue Aufgaben jeden Tag
2. **Team Competitions:** Abteilung vs Abteilung
3. **Seasonal Events:** Weihnachts-Challenge etc.
4. **Badges:** Sichtbar im Profil

---

## 📊 SUCCESS METRICS

- **Engagement:** 85% Daily Active Users
- **Activities:** +40% Calls/Meetings
- **Competition:** Gesunder Wettbewerb
- **Retention:** +20% Mitarbeiterbindung

---

## 🚀 ROLLOUT STRATEGY

**Phase 1:** Basic XP & Level System  
**Phase 2:** Achievements & Badges  
**Phase 3:** Leaderboards & Competition  
**Phase 4:** Advanced Gamification  

---

**Game Design Doc:** [GAMIFICATION_DESIGN.md](./GAMIFICATION_DESIGN.md)  
**Achievement Catalog:** [ACHIEVEMENT_LIST.md](./ACHIEVEMENT_LIST.md)