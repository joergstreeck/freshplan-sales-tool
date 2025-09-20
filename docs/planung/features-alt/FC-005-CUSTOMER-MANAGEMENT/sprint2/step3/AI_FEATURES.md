# 🤖 AI Features - Intelligente Assistenz für Kundenbeziehungen

**Phase:** 3 - Compliance & Ethics  
**Tag:** 3 der Woche 3  
**Status:** 📋 Specification Ready  

## 🧭 Navigation

**← Zurück:** [Contact Analytics](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/step3/CONTACT_ANALYTICS.md)  
**↑ Übergeordnet:** [Step 3 Main Guide](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/step3/README.md)  

## 🎯 Vision: KI als Beziehungsassistent

**AI Features** erweitern Contact Management um **intelligente Automatisierung**:

> "KI die mitdenkt, vorausschaut und den Vertrieb unterstützt - nicht ersetzt"

## 🧠 AI Architecture

### AI Feature Categories

```typescript
// types/ai.types.ts
export interface AIFeatures {
  // Natural Language Processing
  nlp: {
    sentimentAnalysis: SentimentAnalysis;
    keyPhraseExtraction: KeyPhrase[];
    intentRecognition: Intent;
    languageDetection: Language;
  };
  
  // Smart Suggestions
  suggestions: {
    communicationTiming: CommunicationSuggestion;
    messageTemplates: MessageTemplate[];
    talkingPoints: TalkingPoint[];
    meetingAgenda: AgendaItem[];
  };
  
  // Automated Insights
  insights: {
    relationshipSummary: string;
    behaviorPatterns: BehaviorPattern[];
    preferenceProfile: PreferenceProfile;
    riskIndicators: RiskIndicator[];
  };
  
  // Predictive Features
  predictions: {
    nextInteractionDate: Date;
    preferredChannel: CommunicationChannel;
    optimalTimeSlot: TimeSlot;
    topicInterests: Topic[];
  };
  
  // Content Generation
  contentGeneration: {
    emailDrafts: EmailDraft[];
    followUpSuggestions: FollowUp[];
    birthdayMessages: PersonalizedMessage[];
    seasonalGreetings: SeasonalMessage[];
  };
}

export interface SentimentAnalysis {
  overallSentiment: 'positive' | 'neutral' | 'negative';
  score: number; // -1 to 1
  emotions: {
    joy: number;
    trust: number;
    fear: number;
    surprise: number;
    sadness: number;
    disgust: number;
    anger: number;
    anticipation: number;
  };
  confidence: number;
}
```

## 🎨 AI-Powered Components

### AI Assistant Panel

```typescript
// components/AIAssistantPanel.tsx
import React, { useState, useEffect } from 'react';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Chip,
  Button,
  Stack,
  Alert,
  IconButton,
  Tooltip,
  Divider,
  LinearProgress,
  Accordion,
  AccordionSummary,
  AccordionDetails,
  TextField,
  MenuItem
} from '@mui/material';
import {
  AutoAwesome as AIIcon,
  Psychology as InsightIcon,
  Schedule as TimingIcon,
  Email as EmailIcon,
  Refresh as RefreshIcon,
  ThumbUp as ThumbUpIcon,
  ThumbDown as ThumbDownIcon,
  ExpandMore as ExpandMoreIcon
} from '@mui/icons-material';
import { useAIAssistant } from '../hooks/useAIAssistant';

export const AIAssistantPanel: React.FC<{
  contact: Contact;
  interactions: ContactInteraction[];
  onSuggestionApply: (suggestion: AISuggestion) => void;
}> = ({ contact, interactions, onSuggestionApply }) => {
  const [activeFeature, setActiveFeature] = useState<'insights' | 'suggestions' | 'generation'>('insights');
  const [feedback, setFeedback] = useState<Record<string, 'positive' | 'negative'>>({});
  
  const {
    data: aiData,
    isLoading,
    error,
    refetch
  } = useAIAssistant(contact.id, interactions);
  
  const handleFeedback = (suggestionId: string, type: 'positive' | 'negative') => {
    setFeedback(prev => ({ ...prev, [suggestionId]: type }));
    // Send feedback to backend for model improvement
    aiApi.submitFeedback(suggestionId, type);
  };
  
  if (isLoading) {
    return (
      <Card>
        <CardContent>
          <Box display="flex" alignItems="center" gap={2}>
            <AIIcon color="primary" />
            <Typography variant="h6">KI-Assistent analysiert...</Typography>
          </Box>
          <LinearProgress sx={{ mt: 2 }} />
        </CardContent>
      </Card>
    );
  }
  
  if (error || !aiData) {
    return (
      <Alert severity="error">
        KI-Assistent konnte nicht geladen werden
      </Alert>
    );
  }
  
  return (
    <Card>
      <CardContent>
        {/* Header */}
        <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
          <Box display="flex" alignItems="center" gap={1}>
            <AIIcon color="primary" />
            <Typography variant="h6">
              KI-Assistent für {contact.firstName}
            </Typography>
          </Box>
          
          <IconButton onClick={() => refetch()} size="small">
            <RefreshIcon />
          </IconButton>
        </Box>
        
        {/* Feature Tabs */}
        <Stack direction="row" spacing={1} mb={3}>
          <Chip
            label="Insights"
            icon={<InsightIcon />}
            onClick={() => setActiveFeature('insights')}
            color={activeFeature === 'insights' ? 'primary' : 'default'}
            clickable
          />
          <Chip
            label="Vorschläge"
            icon={<TimingIcon />}
            onClick={() => setActiveFeature('suggestions')}
            color={activeFeature === 'suggestions' ? 'primary' : 'default'}
            clickable
          />
          <Chip
            label="Generierung"
            icon={<EmailIcon />}
            onClick={() => setActiveFeature('generation')}
            color={activeFeature === 'generation' ? 'primary' : 'default'}
            clickable
          />
        </Stack>
        
        {/* Active Feature Content */}
        {activeFeature === 'insights' && (
          <AIInsights
            insights={aiData.insights}
            nlp={aiData.nlp}
            onFeedback={handleFeedback}
          />
        )}
        
        {activeFeature === 'suggestions' && (
          <AISuggestions
            suggestions={aiData.suggestions}
            predictions={aiData.predictions}
            onApply={onSuggestionApply}
            onFeedback={handleFeedback}
          />
        )}
        
        {activeFeature === 'generation' && (
          <AIContentGeneration
            contact={contact}
            contentGeneration={aiData.contentGeneration}
            onApply={onSuggestionApply}
            onFeedback={handleFeedback}
          />
        )}
      </CardContent>
    </Card>
  );
};

// AI Insights Component
const AIInsights: React.FC<{
  insights: AIInsights;
  nlp: NLPAnalysis;
  onFeedback: (id: string, type: 'positive' | 'negative') => void;
}> = ({ insights, nlp, onFeedback }) => {
  return (
    <Box>
      {/* Relationship Summary */}
      <Alert
        severity="info"
        sx={{ mb: 2 }}
        action={
          <Stack direction="row" spacing={1}>
            <IconButton
              size="small"
              onClick={() => onFeedback('summary', 'positive')}
            >
              <ThumbUpIcon fontSize="small" />
            </IconButton>
            <IconButton
              size="small"
              onClick={() => onFeedback('summary', 'negative')}
            >
              <ThumbDownIcon fontSize="small" />
            </IconButton>
          </Stack>
        }
      >
        <AlertTitle>Beziehungs-Zusammenfassung</AlertTitle>
        {insights.relationshipSummary}
      </Alert>
      
      {/* Sentiment Analysis */}
      <Box mb={3}>
        <Typography variant="subtitle2" gutterBottom>
          Stimmungsanalyse (letzte 30 Tage)
        </Typography>
        <Box display="flex" alignItems="center" gap={2}>
          <SentimentIndicator sentiment={nlp.sentimentAnalysis} />
          <Typography variant="body2" color="text.secondary">
            Konfidenz: {Math.round(nlp.sentimentAnalysis.confidence * 100)}%
          </Typography>
        </Box>
      </Box>
      
      {/* Behavior Patterns */}
      <Typography variant="subtitle2" gutterBottom>
        Erkannte Verhaltensmuster
      </Typography>
      <Stack spacing={1} mb={3}>
        {insights.behaviorPatterns.map((pattern, index) => (
          <Chip
            key={index}
            label={pattern.description}
            size="small"
            icon={<InsightIcon />}
            variant="outlined"
          />
        ))}
      </Stack>
      
      {/* Risk Indicators */}
      {insights.riskIndicators.length > 0 && (
        <Alert severity="warning">
          <AlertTitle>Risiko-Indikatoren</AlertTitle>
          <ul style={{ margin: 0, paddingLeft: 20 }}>
            {insights.riskIndicators.map((risk, index) => (
              <li key={index}>
                <Typography variant="body2">
                  {risk.description} (Wahrscheinlichkeit: {risk.probability}%)
                </Typography>
              </li>
            ))}
          </ul>
        </Alert>
      )}
    </Box>
  );
};
```

### Smart Email Generation

```typescript
// components/SmartEmailGenerator.tsx
export const SmartEmailGenerator: React.FC<{
  contact: Contact;
  context: EmailContext;
  onGenerate: (draft: EmailDraft) => void;
}> = ({ contact, context, onGenerate }) => {
  const [tone, setTone] = useState<'formal' | 'casual' | 'friendly'>('friendly');
  const [purpose, setPurpose] = useState<EmailPurpose>('follow-up');
  const [includePersonalization, setIncludePersonalization] = useState(true);
  const [generating, setGenerating] = useState(false);
  const [preview, setPreview] = useState<EmailDraft | null>(null);
  
  const handleGenerate = async () => {
    setGenerating(true);
    
    try {
      const draft = await aiApi.generateEmail({
        contactId: contact.id,
        purpose,
        tone,
        context,
        personalization: includePersonalization ? {
          includeHobbies: true,
          includeLastInteraction: true,
          includeUpcomingBirthday: true,
          includeLocationReference: true
        } : undefined
      });
      
      setPreview(draft);
    } finally {
      setGenerating(false);
    }
  };
  
  const handleApply = () => {
    if (preview) {
      onGenerate(preview);
      setPreview(null);
    }
  };
  
  return (
    <Box>
      <Typography variant="h6" gutterBottom>
        E-Mail Generator
      </Typography>
      
      {/* Configuration */}
      <Grid container spacing={2} mb={3}>
        <Grid item xs={12} sm={6}>
          <TextField
            select
            fullWidth
            label="Zweck"
            value={purpose}
            onChange={(e) => setPurpose(e.target.value as EmailPurpose)}
          >
            <MenuItem value="follow-up">Follow-up</MenuItem>
            <MenuItem value="introduction">Vorstellung</MenuItem>
            <MenuItem value="offer">Angebot</MenuItem>
            <MenuItem value="thank-you">Danksagung</MenuItem>
            <MenuItem value="invitation">Einladung</MenuItem>
            <MenuItem value="birthday">Geburtstag</MenuItem>
          </TextField>
        </Grid>
        
        <Grid item xs={12} sm={6}>
          <TextField
            select
            fullWidth
            label="Tonalität"
            value={tone}
            onChange={(e) => setTone(e.target.value as typeof tone)}
          >
            <MenuItem value="formal">Formell</MenuItem>
            <MenuItem value="casual">Locker</MenuItem>
            <MenuItem value="friendly">Freundlich</MenuItem>
          </TextField>
        </Grid>
      </Grid>
      
      <FormControlLabel
        control={
          <Switch
            checked={includePersonalization}
            onChange={(e) => setIncludePersonalization(e.target.checked)}
          />
        }
        label="Personalisierung einbeziehen"
      />
      
      <Button
        variant="contained"
        onClick={handleGenerate}
        disabled={generating}
        startIcon={<AIIcon />}
        fullWidth
        sx={{ mt: 2 }}
      >
        {generating ? 'Generiere...' : 'E-Mail generieren'}
      </Button>
      
      {/* Preview */}
      {preview && (
        <Card sx={{ mt: 3, bgcolor: 'grey.50' }}>
          <CardContent>
            <Typography variant="subtitle2" gutterBottom>
              Betreff: {preview.subject}
            </Typography>
            
            <Divider sx={{ my: 1 }} />
            
            <Typography
              variant="body2"
              sx={{ whiteSpace: 'pre-wrap' }}
            >
              {preview.body}
            </Typography>
            
            <Box display="flex" gap={1} mt={2}>
              <Button
                variant="contained"
                size="small"
                onClick={handleApply}
              >
                Verwenden
              </Button>
              <Button
                variant="outlined"
                size="small"
                onClick={() => handleGenerate()}
              >
                Neu generieren
              </Button>
              <Button
                size="small"
                onClick={() => setPreview(null)}
              >
                Verwerfen
              </Button>
            </Box>
          </CardContent>
        </Card>
      )}
    </Box>
  );
};
```

### AI-Powered Search

```typescript
// components/AISearchBar.tsx
export const AISearchBar: React.FC<{
  onSearch: (results: AISearchResults) => void;
}> = ({ onSearch }) => {
  const [query, setQuery] = useState('');
  const [searching, setSearching] = useState(false);
  const [suggestions, setSuggestions] = useState<string[]>([]);
  
  // Semantic search with natural language
  const handleSearch = async () => {
    if (!query.trim()) return;
    
    setSearching(true);
    
    try {
      const results = await aiApi.semanticSearch({
        query,
        includeContacts: true,
        includeInteractions: true,
        includeDocuments: true,
        maxResults: 20
      });
      
      onSearch(results);
    } finally {
      setSearching(false);
    }
  };
  
  // Auto-complete suggestions
  const handleQueryChange = useMemo(
    () => debounce(async (value: string) => {
      if (value.length < 3) {
        setSuggestions([]);
        return;
      }
      
      const autoComplete = await aiApi.getSearchSuggestions(value);
      setSuggestions(autoComplete.suggestions);
    }, 300),
    []
  );
  
  return (
    <Autocomplete
      freeSolo
      options={suggestions}
      value={query}
      onInputChange={(_, value) => {
        setQuery(value);
        handleQueryChange(value);
      }}
      renderInput={(params) => (
        <TextField
          {...params}
          placeholder="Natürliche Suche: 'Kontakte die länger nicht kontaktiert wurden' oder 'Kunden mit Geburtstag im März'"
          fullWidth
          InputProps={{
            ...params.InputProps,
            startAdornment: <SearchIcon />,
            endAdornment: (
              <>
                {searching && <CircularProgress size={20} />}
                {params.InputProps.endAdornment}
              </>
            )
          }}
          onKeyPress={(e) => {
            if (e.key === 'Enter') {
              handleSearch();
            }
          }}
        />
      )}
      renderOption={(props, option) => (
        <li {...props}>
          <AIIcon sx={{ mr: 1, color: 'text.secondary' }} />
          {option}
        </li>
      )}
    />
  );
};
```

## 🔧 Backend AI Services

### AI Integration Service

```java
// AIIntegrationService.java
@ApplicationScoped
public class AIIntegrationService {
    
    @Inject
    OpenAIClient openAIClient;
    
    @Inject
    ContactRepository contactRepository;
    
    @Inject
    InteractionRepository interactionRepository;
    
    @ConfigProperty(name = "ai.model.name")
    String modelName;
    
    @ConfigProperty(name = "ai.temperature")
    Double temperature;
    
    /**
     * Generate comprehensive AI insights for a contact
     */
    public AIFeatures generateAIFeatures(UUID contactId) {
        Contact contact = contactRepository.findById(contactId)
            .orElseThrow(() -> new NotFoundException("Contact not found"));
        
        List<ContactInteraction> interactions = interactionRepository
            .findByContactId(contactId);
        
        AIFeatures features = new AIFeatures();
        
        // NLP Analysis
        features.setNlp(analyzeInteractions(interactions));
        
        // Generate suggestions
        features.setSuggestions(generateSuggestions(contact, interactions));
        
        // Create insights
        features.setInsights(generateInsights(contact, interactions));
        
        // Predictions
        features.setPredictions(generatePredictions(contact, interactions));
        
        // Content generation templates
        features.setContentGeneration(prepareContentTemplates(contact));
        
        return features;
    }
    
    private NLPAnalysis analyzeInteractions(List<ContactInteraction> interactions) {
        if (interactions.isEmpty()) {
            return NLPAnalysis.neutral();
        }
        
        // Aggregate all interaction notes
        String aggregatedText = interactions.stream()
            .map(ContactInteraction::getNotes)
            .filter(Objects::nonNull)
            .collect(Collectors.joining(" "));
        
        // Call OpenAI for sentiment analysis
        ChatCompletionRequest request = ChatCompletionRequest.builder()
            .model(modelName)
            .messages(List.of(
                new ChatMessage("system", 
                    "You are a sentiment analysis expert. Analyze the following customer interaction notes and provide sentiment analysis in JSON format."),
                new ChatMessage("user", aggregatedText)
            ))
            .temperature(0.3)
            .responseFormat(ResponseFormat.JSON_OBJECT)
            .build();
        
        ChatCompletionResponse response = openAIClient.createChatCompletion(request);
        
        return parseSentimentResponse(response);
    }
    
    /**
     * Generate email draft using AI
     */
    public EmailDraft generateEmail(EmailGenerationRequest request) {
        Contact contact = contactRepository.findById(request.getContactId())
            .orElseThrow(() -> new NotFoundException("Contact not found"));
        
        // Build context
        String context = buildEmailContext(contact, request);
        
        // Create prompt
        String prompt = String.format(
            "Generate a %s email in %s tone for %s %s.\n" +
            "Purpose: %s\n" +
            "Context: %s\n" +
            "Include personalization: %s\n" +
            "Output format: JSON with 'subject' and 'body' fields.",
            request.getPurpose(),
            request.getTone(),
            contact.getFirstName(),
            contact.getLastName(),
            request.getPurpose(),
            context,
            request.getPersonalization() != null ? "Yes" : "No"
        );
        
        ChatCompletionRequest aiRequest = ChatCompletionRequest.builder()
            .model(modelName)
            .messages(List.of(
                new ChatMessage("system", 
                    "You are a professional sales email writer. Generate emails that are engaging, personalized, and effective."),
                new ChatMessage("user", prompt)
            ))
            .temperature(temperature)
            .responseFormat(ResponseFormat.JSON_OBJECT)
            .build();
        
        ChatCompletionResponse response = openAIClient.createChatCompletion(aiRequest);
        
        return parseEmailResponse(response, contact);
    }
    
    /**
     * Semantic search across contacts and interactions
     */
    public AISearchResults semanticSearch(SearchRequest request) {
        // Generate embedding for search query
        EmbeddingRequest embeddingRequest = EmbeddingRequest.builder()
            .model("text-embedding-3-small")
            .input(request.getQuery())
            .build();
        
        EmbeddingResponse embeddingResponse = openAIClient.createEmbedding(embeddingRequest);
        List<Double> queryEmbedding = embeddingResponse.getData().get(0).getEmbedding();
        
        AISearchResults results = new AISearchResults();
        
        // Search contacts by semantic similarity
        if (request.isIncludeContacts()) {
            List<ContactSearchResult> contactResults = searchContactsByEmbedding(
                queryEmbedding,
                request.getMaxResults()
            );
            results.setContacts(contactResults);
        }
        
        // Search interactions
        if (request.isIncludeInteractions()) {
            List<InteractionSearchResult> interactionResults = searchInteractionsByEmbedding(
                queryEmbedding,
                request.getMaxResults()
            );
            results.setInteractions(interactionResults);
        }
        
        // Explain search intent
        results.setInterpretedQuery(interpretSearchQuery(request.getQuery()));
        
        return results;
    }
    
    private String interpretSearchQuery(String query) {
        ChatCompletionRequest request = ChatCompletionRequest.builder()
            .model(modelName)
            .messages(List.of(
                new ChatMessage("system", 
                    "Interpret the user's search query and explain what they are looking for in simple terms."),
                new ChatMessage("user", 
                    "Query: " + query + "\nWhat is the user searching for?")
            ))
            .temperature(0.3)
            .maxTokens(100)
            .build();
        
        ChatCompletionResponse response = openAIClient.createChatCompletion(request);
        return response.getChoices().get(0).getMessage().getContent();
    }
}
```

### AI Model Training Service

```java
// AIModelTrainingService.java
@ApplicationScoped
public class AIModelTrainingService {
    
    @Inject
    FeedbackRepository feedbackRepository;
    
    @Inject
    @CacheName("ai-model-cache")
    Cache<String, AIModel> modelCache;
    
    /**
     * Fine-tune models based on user feedback
     */
    @Scheduled(every = "24h")
    void updateModelsFromFeedback() {
        // Collect feedback data
        List<AIFeedback> recentFeedback = feedbackRepository
            .findRecentFeedback(7); // Last 7 days
        
        if (recentFeedback.size() < 100) {
            return; // Not enough data for training
        }
        
        // Group by feature type
        Map<AIFeatureType, List<AIFeedback>> feedbackByType = recentFeedback.stream()
            .collect(Collectors.groupingBy(AIFeedback::getFeatureType));
        
        // Update each model
        feedbackByType.forEach((featureType, feedbackList) -> {
            updateModel(featureType, feedbackList);
        });
    }
    
    private void updateModel(AIFeatureType featureType, List<AIFeedback> feedback) {
        // Calculate performance metrics
        double accuracy = calculateAccuracy(feedback);
        double satisfaction = calculateSatisfaction(feedback);
        
        if (accuracy < 0.7 || satisfaction < 0.7) {
            // Model needs improvement
            List<TrainingExample> examples = prepareTrainingData(feedback);
            
            // Fine-tune model
            FineTuningRequest request = FineTuningRequest.builder()
                .model(getBaseModel(featureType))
                .trainingExamples(examples)
                .validationSplit(0.2)
                .epochs(3)
                .build();
            
            FineTuningJob job = openAIClient.createFineTuningJob(request);
            
            // Monitor job and update model when complete
            monitorFineTuningJob(job.getId(), featureType);
        }
    }
}
```

## 🧪 AI Feature Testing

```typescript
// __tests__/ai-features.test.ts
describe('AI Features', () => {
  it('should generate relevant email draft', async () => {
    const contact = createMockContact({
      firstName: 'John',
      lastName: 'Doe',
      hobbies: ['Golf', 'Kochen']
    });
    
    const draft = await aiApi.generateEmail({
      contactId: contact.id,
      purpose: 'follow-up',
      tone: 'friendly',
      personalization: {
        includeHobbies: true
      }
    });
    
    expect(draft.subject).toContain('John');
    expect(draft.body).toMatch(/Golf|Kochen/);
    expect(draft.body.length).toBeGreaterThan(100);
  });
  
  it('should provide accurate sentiment analysis', async () => {
    const interactions = [
      createMockInteraction({ notes: 'Very happy with our service!' }),
      createMockInteraction({ notes: 'Excellent meeting, looking forward to next steps' })
    ];
    
    const analysis = await aiApi.analyzeSentiment(interactions);
    
    expect(analysis.overallSentiment).toBe('positive');
    expect(analysis.score).toBeGreaterThan(0.7);
  });
  
  it('should handle natural language search', async () => {
    const results = await aiApi.semanticSearch({
      query: 'Kunden die seit mehr als 30 Tagen nicht kontaktiert wurden',
      includeContacts: true
    });
    
    expect(results.interpretedQuery).toContain('inactive');
    expect(results.contacts.length).toBeGreaterThan(0);
  });
});
```

## 🎯 Success Metrics

### AI Adoption:
- **Feature Usage:** > 60% daily active users
- **Suggestion Acceptance:** > 40% applied
- **Email Generation:** > 100/month

### Quality Metrics:
- **Sentiment Accuracy:** > 85%
- **Email Relevance:** > 4.0/5 rating
- **Search Precision:** > 80%

### Business Impact:
- **Response Time:** -50% with templates
- **Engagement Rate:** +30% with AI emails
- **Productivity:** +25% time saved

---

**↑ Zurück zu:** [Step 3 Main Guide](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/step3/README.md)

**AI + Human Touch = Perfect Balance! 🤖✨**