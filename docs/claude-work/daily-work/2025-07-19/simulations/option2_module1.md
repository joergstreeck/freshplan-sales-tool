# FC-035-M1: SOCIAL PROFILE INTEGRATION

## 🚀 QUICK START

```typescript
const SocialProfileCard = ({ customer }) => {
  const profiles = useSocialProfiles(customer.id);
  
  return (
    <Card>
      <CardHeader
        title="Social Media Profile"
        action={
          <IconButton onClick={syncProfiles}>
            <SyncIcon />
          </IconButton>
        }
      />
      <CardContent>
        <Stack direction="row" spacing={2}>
          {profiles.linkedin && (
            <Chip
              icon={<LinkedInIcon />}
              label={profiles.linkedin.name}
              component="a"
              href={profiles.linkedin.url}
              clickable
            />
          )}
          {profiles.xing && (
            <Chip
              icon={<BusinessIcon />}
              label="XING"
              component="a"
              href={profiles.xing.url}
              clickable
            />
          )}
        </Stack>
        
        {/* Recent Activity */}
        <Box sx={{ mt: 2 }}>
          <Typography variant="subtitle2">Letzte Aktivität:</Typography>
          {profiles.recentPosts.map(post => (
            <Alert 
              key={post.id} 
              severity="info" 
              sx={{ mt: 1 }}
              action={
                <Button size="small" onClick={() => engageWith(post)}>
                  Reagieren
                </Button>
              }
            >
              {post.summary}
            </Alert>
          ))}
        </Box>
      </CardContent>
    </Card>
  );
};
```

## 📊 DATA MODEL

```typescript
interface SocialProfile {
  id: string;
  customerId: string;
  platform: 'linkedin' | 'xing' | 'twitter';
  profileUrl: string;
  profileName: string;
  lastSynced: Date;
  recentActivity: Activity[];
}

interface Activity {
  id: string;
  type: 'post' | 'comment' | 'share';
  content: string;
  timestamp: Date;
  engagement: {
    likes: number;
    comments: number;
    shares: number;
  };
}
```

## 🔄 SYNC LOGIC

```typescript
const syncProfiles = async (customerId: string) => {
  const profiles = await getCustomerProfiles(customerId);
  
  for (const profile of profiles) {
    if (profile.platform === 'linkedin') {
      const activities = await linkedInAPI.getRecentActivity(profile.profileId);
      await updateProfileActivity(profile.id, activities);
    }
  }
  
  return { success: true, synced: profiles.length };
};
```