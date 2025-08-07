# 🏢 Org-Chart Visualization - Hierarchie-Verständnis auf einen Blick

**Phase:** 2 - Advanced Features  
**Priorität:** 🟡 WICHTIG - Entscheider-Struktur verstehen  
**Status:** 📋 GEPLANT  
**Letzte Aktualisierung:** 08.08.2025  
**Claude-Ready:** ✅ Vollständig navigierbar

## 🧭 NAVIGATION FÜR CLAUDE

**← Zurück:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/COMMUNICATION_HISTORY.md`  
**→ Nächster:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/SOCIAL_MEDIA_INTEGRATION.md`  
**↑ Parent:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/README.md`  
**⚠️ Wichtig für:**
- Vertrieb (Entscheider identifizieren)
- Key Account Management
- Stakeholder Mapping

## ⚡ Quick Implementation Guide für Claude

```bash
# SOFORT STARTEN - Org-Chart Visualization implementieren:
cd /Users/joergstreeck/freshplan-sales-tool

# 1. Backend Hierarchy Services
mkdir -p backend/src/main/java/de/freshplan/hierarchy
touch backend/src/main/java/de/freshplan/hierarchy/service/OrgChartService.java
touch backend/src/main/java/de/freshplan/hierarchy/dto/OrgChartNode.java
touch backend/src/main/java/de/freshplan/hierarchy/resource/OrgChartResource.java

# 2. Frontend Org-Chart Components
mkdir -p frontend/src/features/customers/components/orgchart
touch frontend/src/features/customers/components/orgchart/OrgChartVisualization.tsx
touch frontend/src/features/customers/components/orgchart/OrgChartNode.tsx
touch frontend/src/features/customers/components/orgchart/OrgChartControls.tsx
touch frontend/src/features/customers/components/orgchart/ContactNodeCard.tsx

# 3. D3.js Installation für Visualisierung
cd frontend
npm install d3 @types/d3 react-d3-tree
npm install react-flow-renderer  # Alternative zu D3

# 4. Tests
mkdir -p frontend/src/features/customers/components/orgchart/__tests__
touch frontend/src/features/customers/components/orgchart/__tests__/OrgChart.test.tsx
```

## 🎯 Das Problem: Unklare Entscheider-Strukturen

**Vertriebliche Blindflüge:**
- 🤷 "Wer entscheidet wirklich?" - Ratespiel
- 🔄 "Wer berichtet an wen?" - Verwirrung
- 🎯 "Wen muss ich überzeugen?" - Unsicherheit
- 📊 "Wie ist die Machtverteilung?" - Keine Ahnung

## 💡 DIE LÖSUNG: Interaktive Org-Chart Visualization

### 1. Org-Chart Service Backend

**Datei:** `backend/src/main/java/de/freshplan/hierarchy/service/OrgChartService.java`

```java
// CLAUDE: Service für Hierarchie-Berechnung und Org-Chart Daten
// Pfad: backend/src/main/java/de/freshplan/hierarchy/service/OrgChartService.java

package de.freshplan.hierarchy.service;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class OrgChartService {
    
    @Inject
    CustomerContactRepository contactRepository;
    
    @Inject
    RelationshipIntelligenceService relationshipService;
    
    /**
     * Build complete org chart for a customer
     */
    public OrgChartData buildOrgChart(UUID customerId) {
        List<CustomerContact> contacts = contactRepository.findByCustomerId(customerId);
        
        if (contacts.isEmpty()) {
            return OrgChartData.empty();
        }
        
        // Find root nodes (no reportsTo)
        List<CustomerContact> roots = contacts.stream()
            .filter(c -> c.getReportsTo() == null)
            .collect(Collectors.toList());
        
        // If no explicit hierarchy, try to infer from job titles
        if (roots.isEmpty()) {
            roots = inferRootFromTitles(contacts);
        }
        
        // Build hierarchical structure
        List<OrgChartNode> rootNodes = roots.stream()
            .map(contact -> buildNode(contact, contacts, 0))
            .collect(Collectors.toList());
        
        // Calculate statistics
        OrgChartStats stats = calculateStats(contacts, rootNodes);
        
        // Identify key decision makers
        List<DecisionMaker> decisionMakers = identifyDecisionMakers(contacts);
        
        return OrgChartData.builder()
            .nodes(rootNodes)
            .stats(stats)
            .decisionMakers(decisionMakers)
            .customerId(customerId)
            .build();
    }
    
    /**
     * Build node recursively with children
     */
    private OrgChartNode buildNode(CustomerContact contact, List<CustomerContact> allContacts, int level) {
        // Find direct reports
        List<CustomerContact> directReports = allContacts.stream()
            .filter(c -> contact.getId().equals(c.getReportsTo()))
            .collect(Collectors.toList());
        
        // Build children nodes
        List<OrgChartNode> children = directReports.stream()
            .map(child -> buildNode(child, allContacts, level + 1))
            .collect(Collectors.toList());
        
        // Get relationship warmth
        Float warmthScore = relationshipService.getWarmthScore(contact.getId());
        
        // Calculate influence score
        int influenceScore = calculateInfluenceScore(contact, directReports.size());
        
        return OrgChartNode.builder()
            .id(contact.getId())
            .name(formatName(contact))
            .title(contact.getJobTitle())
            .department(contact.getDepartment())
            .email(contact.getEmail())
            .phone(contact.getPhone())
            .level(level)
            .isPrimary(contact.isPrimary())
            .decisionLevel(contact.getDecisionLevel())
            .warmthScore(warmthScore)
            .influenceScore(influenceScore)
            .directReportsCount(directReports.size())
            .children(children)
            .metadata(buildMetadata(contact))
            .build();
    }
    
    /**
     * Infer hierarchy from job titles when no explicit reporting structure
     */
    private List<CustomerContact> inferRootFromTitles(List<CustomerContact> contacts) {
        // Priority order for executive titles
        String[] executiveTitles = {
            "CEO", "Geschäftsführer", "Managing Director",
            "COO", "CFO", "CTO", "President",
            "Vice President", "VP", "Director", "Direktor",
            "Head of", "Leiter", "Manager"
        };
        
        for (String title : executiveTitles) {
            List<CustomerContact> matches = contacts.stream()
                .filter(c -> c.getJobTitle() != null && 
                        c.getJobTitle().toLowerCase().contains(title.toLowerCase()))
                .collect(Collectors.toList());
            
            if (!matches.isEmpty()) {
                return matches;
            }
        }
        
        // Fallback: return primary contacts or first contact
        return contacts.stream()
            .filter(CustomerContact::isPrimary)
            .findFirst()
            .map(List::of)
            .orElse(List.of(contacts.get(0)));
    }
    
    /**
     * Calculate influence score based on various factors
     */
    private int calculateInfluenceScore(CustomerContact contact, int directReports) {
        int score = 0;
        
        // Decision level weight (0-40 points)
        if (contact.getDecisionLevel() != null) {
            switch (contact.getDecisionLevel()) {
                case EXECUTIVE:
                    score += 40;
                    break;
                case MANAGER:
                    score += 30;
                    break;
                case INFLUENCER:
                    score += 20;
                    break;
                case OPERATIONAL:
                    score += 10;
                    break;
            }
        }
        
        // Direct reports weight (0-30 points)
        score += Math.min(directReports * 5, 30);
        
        // Primary contact bonus (10 points)
        if (contact.isPrimary()) {
            score += 10;
        }
        
        // Job title weight (0-20 points)
        if (contact.getJobTitle() != null) {
            String title = contact.getJobTitle().toLowerCase();
            if (title.contains("ceo") || title.contains("geschäftsführer")) {
                score += 20;
            } else if (title.contains("director") || title.contains("vp")) {
                score += 15;
            } else if (title.contains("manager") || title.contains("leiter")) {
                score += 10;
            } else if (title.contains("lead") || title.contains("senior")) {
                score += 5;
            }
        }
        
        return Math.min(score, 100); // Cap at 100
    }
    
    /**
     * Identify key decision makers
     */
    private List<DecisionMaker> identifyDecisionMakers(List<CustomerContact> contacts) {
        return contacts.stream()
            .filter(c -> c.getDecisionLevel() == DecisionLevel.EXECUTIVE ||
                        c.getDecisionLevel() == DecisionLevel.MANAGER ||
                        c.isPrimary())
            .map(c -> DecisionMaker.builder()
                .contact(c)
                .role(determineRole(c))
                .influence(calculateInfluenceScore(c, countDirectReports(c, contacts)))
                .insights(generateInsights(c))
                .build())
            .sorted(Comparator.comparingInt(DecisionMaker::getInfluence).reversed())
            .collect(Collectors.toList());
    }
    
    /**
     * Determine strategic role
     */
    private StrategicRole determineRole(CustomerContact contact) {
        String title = contact.getJobTitle() != null ? 
            contact.getJobTitle().toLowerCase() : "";
        
        if (title.contains("ceo") || title.contains("geschäftsführer")) {
            return StrategicRole.FINAL_DECISION_MAKER;
        } else if (title.contains("cfo") || title.contains("finance")) {
            return StrategicRole.BUDGET_HOLDER;
        } else if (title.contains("cto") || title.contains("it")) {
            return StrategicRole.TECHNICAL_EVALUATOR;
        } else if (contact.getDecisionLevel() == DecisionLevel.INFLUENCER) {
            return StrategicRole.INFLUENCER;
        } else if (contact.getDecisionLevel() == DecisionLevel.MANAGER) {
            return StrategicRole.RECOMMENDER;
        } else {
            return StrategicRole.USER;
        }
    }
}

// DTOs
public class OrgChartNode {
    public UUID id;
    public String name;
    public String title;
    public String department;
    public String email;
    public String phone;
    public int level;
    public boolean isPrimary;
    public DecisionLevel decisionLevel;
    public Float warmthScore;
    public int influenceScore;
    public int directReportsCount;
    public List<OrgChartNode> children;
    public Map<String, Object> metadata;
}

public class OrgChartStats {
    public int totalContacts;
    public int maxDepth;
    public int executiveCount;
    public int managerCount;
    public Map<String, Integer> departmentCounts;
    public float avgWarmthScore;
    public int orphanedContacts; // Contacts without clear hierarchy
}

public enum StrategicRole {
    FINAL_DECISION_MAKER("Finale Entscheidung"),
    BUDGET_HOLDER("Budget-Verantwortung"),
    TECHNICAL_EVALUATOR("Technische Bewertung"),
    RECOMMENDER("Empfehlung"),
    INFLUENCER("Beeinflusser"),
    USER("Anwender");
    
    private final String displayName;
    
    StrategicRole(String displayName) {
        this.displayName = displayName;
    }
}
```

### 2. Frontend Org-Chart Visualization

**Datei:** `frontend/src/features/customers/components/orgchart/OrgChartVisualization.tsx`

```typescript
// CLAUDE: Interaktive Org-Chart mit D3.js und React
// Pfad: frontend/src/features/customers/components/orgchart/OrgChartVisualization.tsx

import React, { useState, useEffect, useRef, useMemo } from 'react';
import {
  Box,
  Paper,
  Typography,
  IconButton,
  ToggleButton,
  ToggleButtonGroup,
  Slider,
  Switch,
  FormControlLabel,
  Chip,
  Tooltip,
  Card,
  CardContent,
  Avatar,
  Badge,
  Fab,
  Menu,
  MenuItem,
  Divider,
  Button,
  useTheme
} from '@mui/material';
import {
  AccountTree as TreeIcon,
  Hub as NetworkIcon,
  GridView as GridIcon,
  ZoomIn as ZoomInIcon,
  ZoomOut as ZoomOutIcon,
  CenterFocusStrong as CenterIcon,
  Download as DownloadIcon,
  Fullscreen as FullscreenIcon,
  Settings as SettingsIcon,
  Person as PersonIcon,
  Star as StarIcon,
  TrendingUp as InfluenceIcon,
  Thermostat as WarmthIcon,
  Warning as WarningIcon
} from '@mui/icons-material';
import * as d3 from 'd3';
import { OrgChartNode, OrgChartData } from '../../../types/orgchart.types';

interface OrgChartVisualizationProps {
  customerId: string;
  onNodeClick?: (node: OrgChartNode) => void;
  onNodeHover?: (node: OrgChartNode | null) => void;
  height?: number;
}

export const OrgChartVisualization: React.FC<OrgChartVisualizationProps> = ({
  customerId,
  onNodeClick,
  onNodeHover,
  height = 600
}) => {
  const svgRef = useRef<SVGSVGElement>(null);
  const containerRef = useRef<HTMLDivElement>(null);
  const [chartData, setChartData] = useState<OrgChartData | null>(null);
  const [viewMode, setViewMode] = useState<'tree' | 'network' | 'grid'>('tree');
  const [selectedNode, setSelectedNode] = useState<OrgChartNode | null>(null);
  const [hoveredNode, setHoveredNode] = useState<OrgChartNode | null>(null);
  const [zoom, setZoom] = useState(1);
  const [showWarmth, setShowWarmth] = useState(true);
  const [showInfluence, setShowInfluence] = useState(true);
  const [filterLevel, setFilterLevel] = useState<number | null>(null);
  const theme = useTheme();
  
  // Load org chart data
  useEffect(() => {
    loadOrgChartData();
  }, [customerId]);
  
  const loadOrgChartData = async () => {
    try {
      const response = await fetch(`/api/hierarchy/orgchart/${customerId}`);
      const data = await response.json();
      setChartData(data);
    } catch (error) {
      console.error('Failed to load org chart:', error);
    }
  };
  
  // D3 Tree Layout
  useEffect(() => {
    if (!chartData || !svgRef.current || viewMode !== 'tree') return;
    
    const svg = d3.select(svgRef.current);
    const width = containerRef.current?.clientWidth || 800;
    const nodeWidth = 180;
    const nodeHeight = 80;
    
    // Clear previous content
    svg.selectAll('*').remove();
    
    // Create zoom behavior
    const zoomBehavior = d3.zoom()
      .scaleExtent([0.5, 2])
      .on('zoom', (event) => {
        g.attr('transform', event.transform);
        setZoom(event.transform.k);
      });
    
    svg.call(zoomBehavior as any);
    
    // Create main group
    const g = svg.append('g')
      .attr('transform', `translate(${width / 2}, 50)`);
    
    // Create tree layout
    const treeLayout = d3.tree<OrgChartNode>()
      .nodeSize([nodeWidth + 20, nodeHeight + 40])
      .separation((a, b) => a.parent === b.parent ? 1 : 1.5);
    
    // Convert data to hierarchy
    const root = d3.hierarchy(chartData.nodes[0]);
    const treeData = treeLayout(root);
    
    // Draw links
    const links = g.selectAll('.link')
      .data(treeData.links())
      .enter()
      .append('path')
      .attr('class', 'link')
      .attr('d', d3.linkVertical()
        .x((d: any) => d.x)
        .y((d: any) => d.y) as any)
      .style('fill', 'none')
      .style('stroke', theme.palette.divider)
      .style('stroke-width', 2);
    
    // Draw nodes
    const nodes = g.selectAll('.node')
      .data(treeData.descendants())
      .enter()
      .append('g')
      .attr('class', 'node')
      .attr('transform', (d: any) => `translate(${d.x}, ${d.y})`);
    
    // Add rectangles for nodes
    nodes.append('rect')
      .attr('x', -nodeWidth / 2)
      .attr('y', -nodeHeight / 2)
      .attr('width', nodeWidth)
      .attr('height', nodeHeight)
      .attr('rx', 8)
      .style('fill', (d: any) => {
        if (showWarmth && d.data.warmthScore) {
          // Color based on warmth
          const warmth = d.data.warmthScore;
          if (warmth > 75) return '#4CAF50';
          if (warmth > 50) return '#FFC107';
          if (warmth > 25) return '#FF9800';
          return '#F44336';
        }
        return theme.palette.background.paper;
      })
      .style('stroke', (d: any) => {
        if (d.data.isPrimary) return theme.palette.primary.main;
        if (d.data.decisionLevel === 'EXECUTIVE') return '#FFD700';
        return theme.palette.divider;
      })
      .style('stroke-width', (d: any) => d.data.isPrimary ? 3 : 2)
      .style('cursor', 'pointer')
      .on('click', (event, d: any) => {
        setSelectedNode(d.data);
        onNodeClick?.(d.data);
      })
      .on('mouseenter', (event, d: any) => {
        setHoveredNode(d.data);
        onNodeHover?.(d.data);
      })
      .on('mouseleave', () => {
        setHoveredNode(null);
        onNodeHover?.(null);
      });
    
    // Add text for names
    nodes.append('text')
      .attr('dy', -10)
      .attr('text-anchor', 'middle')
      .style('font-weight', 'bold')
      .style('font-size', '14px')
      .style('fill', theme.palette.text.primary)
      .text((d: any) => d.data.name);
    
    // Add text for titles
    nodes.append('text')
      .attr('dy', 8)
      .attr('text-anchor', 'middle')
      .style('font-size', '12px')
      .style('fill', theme.palette.text.secondary)
      .text((d: any) => d.data.title || '');
    
    // Add department
    nodes.append('text')
      .attr('dy', 24)
      .attr('text-anchor', 'middle')
      .style('font-size', '11px')
      .style('fill', theme.palette.text.disabled)
      .text((d: any) => d.data.department || '');
    
    // Add badges for influence
    if (showInfluence) {
      nodes.append('circle')
        .attr('cx', nodeWidth / 2 - 10)
        .attr('cy', -nodeHeight / 2 + 10)
        .attr('r', 12)
        .style('fill', theme.palette.primary.main)
        .style('display', (d: any) => d.data.influenceScore > 50 ? 'block' : 'none');
      
      nodes.append('text')
        .attr('x', nodeWidth / 2 - 10)
        .attr('y', -nodeHeight / 2 + 14)
        .attr('text-anchor', 'middle')
        .style('font-size', '10px')
        .style('fill', 'white')
        .style('font-weight', 'bold')
        .text((d: any) => d.data.influenceScore > 50 ? d.data.influenceScore : '');
    }
    
    // Add star for primary contact
    nodes.append('text')
      .attr('x', -nodeWidth / 2 + 10)
      .attr('y', -nodeHeight / 2 + 20)
      .style('font-size', '16px')
      .style('fill', '#FFD700')
      .style('display', (d: any) => d.data.isPrimary ? 'block' : 'none')
      .text('⭐');
    
  }, [chartData, viewMode, showWarmth, showInfluence, theme]);
  
  // Calculate metrics
  const metrics = useMemo(() => {
    if (!chartData) return null;
    
    return {
      totalContacts: chartData.stats.totalContacts,
      maxDepth: chartData.stats.maxDepth,
      executiveCount: chartData.stats.executiveCount,
      avgWarmth: chartData.stats.avgWarmthScore,
      topInfluencer: chartData.decisionMakers[0]
    };
  }, [chartData]);
  
  // Export functions
  const exportAsPNG = () => {
    if (!svgRef.current) return;
    
    // Convert SVG to PNG using canvas
    const svgData = new XMLSerializer().serializeToString(svgRef.current);
    const canvas = document.createElement('canvas');
    const ctx = canvas.getContext('2d');
    const img = new Image();
    
    img.onload = () => {
      canvas.width = img.width;
      canvas.height = img.height;
      ctx?.drawImage(img, 0, 0);
      canvas.toBlob((blob) => {
        if (blob) {
          const url = URL.createObjectURL(blob);
          const a = document.createElement('a');
          a.href = url;
          a.download = `orgchart_${customerId}.png`;
          a.click();
        }
      });
    };
    
    img.src = 'data:image/svg+xml;base64,' + btoa(svgData);
  };
  
  const exportAsJSON = () => {
    const dataStr = JSON.stringify(chartData, null, 2);
    const dataUri = 'data:application/json;charset=utf-8,'+ encodeURIComponent(dataStr);
    const a = document.createElement('a');
    a.href = dataUri;
    a.download = `orgchart_${customerId}.json`;
    a.click();
  };
  
  return (
    <Box>
      {/* Header & Controls */}
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
        <Typography variant="h5">
          Organisations-Struktur
        </Typography>
        
        <Box display="flex" gap={2} alignItems="center">
          {/* View Mode Toggle */}
          <ToggleButtonGroup
            value={viewMode}
            exclusive
            onChange={(_, value) => value && setViewMode(value)}
            size="small"
          >
            <ToggleButton value="tree">
              <Tooltip title="Baum-Ansicht">
                <TreeIcon />
              </Tooltip>
            </ToggleButton>
            <ToggleButton value="network">
              <Tooltip title="Netzwerk-Ansicht">
                <NetworkIcon />
              </Tooltip>
            </ToggleButton>
            <ToggleButton value="grid">
              <Tooltip title="Grid-Ansicht">
                <GridIcon />
              </Tooltip>
            </ToggleButton>
          </ToggleButtonGroup>
          
          {/* Display Options */}
          <FormControlLabel
            control={
              <Switch
                checked={showWarmth}
                onChange={(e) => setShowWarmth(e.target.checked)}
                size="small"
              />
            }
            label="Warmth"
          />
          
          <FormControlLabel
            control={
              <Switch
                checked={showInfluence}
                onChange={(e) => setShowInfluence(e.target.checked)}
                size="small"
              />
            }
            label="Einfluss"
          />
          
          {/* Zoom Controls */}
          <Box display="flex" alignItems="center" gap={1}>
            <IconButton size="small" onClick={() => setZoom(zoom * 0.8)}>
              <ZoomOutIcon />
            </IconButton>
            <Typography variant="body2">{Math.round(zoom * 100)}%</Typography>
            <IconButton size="small" onClick={() => setZoom(zoom * 1.2)}>
              <ZoomInIcon />
            </IconButton>
          </Box>
          
          {/* Export Menu */}
          <IconButton onClick={exportAsPNG}>
            <DownloadIcon />
          </IconButton>
        </Box>
      </Box>
      
      {/* Metrics Bar */}
      {metrics && (
        <Box display="flex" gap={2} mb={2}>
          <Chip
            icon={<PersonIcon />}
            label={`${metrics.totalContacts} Kontakte`}
            variant="outlined"
          />
          <Chip
            icon={<TreeIcon />}
            label={`${metrics.maxDepth} Ebenen`}
            variant="outlined"
          />
          <Chip
            icon={<StarIcon />}
            label={`${metrics.executiveCount} Executives`}
            variant="outlined"
            color="primary"
          />
          <Chip
            icon={<WarmthIcon />}
            label={`⌀ ${Math.round(metrics.avgWarmth)}% Warmth`}
            variant="outlined"
            color={metrics.avgWarmth > 50 ? 'success' : 'warning'}
          />
        </Box>
      )}
      
      {/* Chart Container */}
      <Paper
        ref={containerRef}
        sx={{
          height,
          overflow: 'hidden',
          position: 'relative',
          bgcolor: 'background.default'
        }}
      >
        {viewMode === 'tree' && (
          <svg
            ref={svgRef}
            width="100%"
            height="100%"
            style={{ cursor: 'grab' }}
          />
        )}
        
        {viewMode === 'grid' && chartData && (
          <OrgChartGrid
            data={chartData}
            onNodeClick={onNodeClick}
            showWarmth={showWarmth}
            showInfluence={showInfluence}
          />
        )}
        
        {/* Node Details Overlay */}
        {hoveredNode && (
          <Paper
            sx={{
              position: 'absolute',
              top: 16,
              right: 16,
              p: 2,
              minWidth: 250,
              boxShadow: 3
            }}
          >
            <Typography variant="h6">{hoveredNode.name}</Typography>
            <Typography variant="body2" color="text.secondary">
              {hoveredNode.title}
            </Typography>
            {hoveredNode.department && (
              <Typography variant="body2" color="text.secondary">
                {hoveredNode.department}
              </Typography>
            )}
            
            <Divider sx={{ my: 1 }} />
            
            <Box display="flex" justifyContent="space-between">
              <Typography variant="body2">Einfluss:</Typography>
              <Chip
                size="small"
                label={`${hoveredNode.influenceScore}%`}
                color={hoveredNode.influenceScore > 70 ? 'success' : 'default'}
              />
            </Box>
            
            {hoveredNode.warmthScore !== undefined && (
              <Box display="flex" justifyContent="space-between" mt={1}>
                <Typography variant="body2">Beziehung:</Typography>
                <Chip
                  size="small"
                  label={`${Math.round(hoveredNode.warmthScore)}%`}
                  color={
                    hoveredNode.warmthScore > 75 ? 'success' :
                    hoveredNode.warmthScore > 50 ? 'warning' : 'error'
                  }
                />
              </Box>
            )}
            
            {hoveredNode.directReportsCount > 0 && (
              <Box display="flex" justifyContent="space-between" mt={1}>
                <Typography variant="body2">Direkte Berichte:</Typography>
                <Typography variant="body2" fontWeight="bold">
                  {hoveredNode.directReportsCount}
                </Typography>
              </Box>
            )}
            
            {hoveredNode.isPrimary && (
              <Chip
                icon={<StarIcon />}
                label="Hauptkontakt"
                size="small"
                color="primary"
                sx={{ mt: 1 }}
              />
            )}
          </Paper>
        )}
      </Paper>
      
      {/* Decision Makers Panel */}
      {chartData && chartData.decisionMakers.length > 0 && (
        <Box mt={3}>
          <Typography variant="h6" gutterBottom>
            Wichtige Entscheider
          </Typography>
          <Grid container spacing={2}>
            {chartData.decisionMakers.slice(0, 4).map(dm => (
              <Grid item xs={12} sm={6} md={3} key={dm.contact.id}>
                <Card>
                  <CardContent>
                    <Box display="flex" alignItems="center" gap={1} mb={1}>
                      <Avatar sx={{ bgcolor: theme.palette.primary.main }}>
                        {dm.contact.firstName?.[0]}{dm.contact.lastName?.[0]}
                      </Avatar>
                      <Box flex={1}>
                        <Typography variant="subtitle2" noWrap>
                          {dm.contact.firstName} {dm.contact.lastName}
                        </Typography>
                        <Typography variant="caption" color="text.secondary" noWrap>
                          {dm.contact.jobTitle}
                        </Typography>
                      </Box>
                    </Box>
                    
                    <Chip
                      size="small"
                      label={dm.role}
                      color="primary"
                      variant="outlined"
                      sx={{ mb: 1 }}
                    />
                    
                    <Box display="flex" justifyContent="space-between">
                      <Typography variant="caption">Einfluss:</Typography>
                      <Typography variant="caption" fontWeight="bold">
                        {dm.influence}%
                      </Typography>
                    </Box>
                    
                    {dm.insights && dm.insights.length > 0 && (
                      <Typography variant="caption" color="text.secondary" sx={{ mt: 1, display: 'block' }}>
                        💡 {dm.insights[0]}
                      </Typography>
                    )}
                  </CardContent>
                </Card>
              </Grid>
            ))}
          </Grid>
        </Box>
      )}
    </Box>
  );
};
```

### 3. Grid View Component

**Datei:** `frontend/src/features/customers/components/orgchart/OrgChartGrid.tsx`

```typescript
// CLAUDE: Grid-basierte Org-Chart Alternative
// Pfad: frontend/src/features/customers/components/orgchart/OrgChartGrid.tsx

import React from 'react';
import {
  Box,
  Card,
  CardContent,
  Grid,
  Typography,
  Avatar,
  Chip,
  LinearProgress
} from '@mui/material';

interface OrgChartGridProps {
  data: OrgChartData;
  onNodeClick?: (node: OrgChartNode) => void;
  showWarmth: boolean;
  showInfluence: boolean;
}

export const OrgChartGrid: React.FC<OrgChartGridProps> = ({
  data,
  onNodeClick,
  showWarmth,
  showInfluence
}) => {
  // Group contacts by level
  const levelGroups = new Map<number, OrgChartNode[]>();
  
  const collectNodes = (node: OrgChartNode) => {
    const level = node.level || 0;
    if (!levelGroups.has(level)) {
      levelGroups.set(level, []);
    }
    levelGroups.get(level)!.push(node);
    
    if (node.children) {
      node.children.forEach(collectNodes);
    }
  };
  
  data.nodes.forEach(collectNodes);
  
  // Sort levels
  const sortedLevels = Array.from(levelGroups.keys()).sort((a, b) => a - b);
  
  return (
    <Box p={3}>
      {sortedLevels.map(level => (
        <Box key={level} mb={4}>
          <Typography variant="h6" gutterBottom color="primary">
            Ebene {level + 1}
          </Typography>
          
          <Grid container spacing={2}>
            {levelGroups.get(level)!.map(node => (
              <Grid item xs={12} sm={6} md={4} lg={3} key={node.id}>
                <Card
                  sx={{
                    cursor: 'pointer',
                    transition: 'all 0.3s',
                    '&:hover': {
                      transform: 'translateY(-4px)',
                      boxShadow: 4
                    },
                    borderLeft: node.isPrimary ? '4px solid' : undefined,
                    borderLeftColor: 'primary.main'
                  }}
                  onClick={() => onNodeClick?.(node)}
                >
                  <CardContent>
                    <Box display="flex" alignItems="center" gap={2} mb={2}>
                      <Avatar
                        sx={{
                          bgcolor: node.decisionLevel === 'EXECUTIVE' ? 
                            'warning.main' : 'primary.main',
                          width: 48,
                          height: 48
                        }}
                      >
                        {node.name.split(' ').map(n => n[0]).join('')}
                      </Avatar>
                      
                      <Box flex={1}>
                        <Typography variant="subtitle1" fontWeight="bold">
                          {node.name}
                        </Typography>
                        <Typography variant="body2" color="text.secondary">
                          {node.title}
                        </Typography>
                      </Box>
                    </Box>
                    
                    {node.department && (
                      <Chip
                        size="small"
                        label={node.department}
                        variant="outlined"
                        sx={{ mb: 2 }}
                      />
                    )}
                    
                    {showWarmth && node.warmthScore !== undefined && (
                      <Box mb={1}>
                        <Box display="flex" justifyContent="space-between">
                          <Typography variant="caption">Beziehung</Typography>
                          <Typography variant="caption">
                            {Math.round(node.warmthScore)}%
                          </Typography>
                        </Box>
                        <LinearProgress
                          variant="determinate"
                          value={node.warmthScore}
                          color={
                            node.warmthScore > 75 ? 'success' :
                            node.warmthScore > 50 ? 'warning' : 'error'
                          }
                        />
                      </Box>
                    )}
                    
                    {showInfluence && (
                      <Box>
                        <Box display="flex" justifyContent="space-between">
                          <Typography variant="caption">Einfluss</Typography>
                          <Typography variant="caption">
                            {node.influenceScore}%
                          </Typography>
                        </Box>
                        <LinearProgress
                          variant="determinate"
                          value={node.influenceScore}
                          color="primary"
                        />
                      </Box>
                    )}
                    
                    {node.directReportsCount > 0 && (
                      <Typography variant="caption" color="text.secondary" sx={{ mt: 1, display: 'block' }}>
                        {node.directReportsCount} direkte Berichte
                      </Typography>
                    )}
                  </CardContent>
                </Card>
              </Grid>
            ))}
          </Grid>
        </Box>
      ))}
    </Box>
  );
};
```

## 📋 IMPLEMENTIERUNGS-CHECKLISTE

### Phase 1: Backend Services (30 Min)
- [ ] OrgChartService implementieren
- [ ] Hierarchy Detection Logic
- [ ] Influence Score Calculation
- [ ] Decision Maker Identification

### Phase 2: D3.js Visualization (60 Min)
- [ ] Tree Layout Implementation
- [ ] Interactive Nodes
- [ ] Zoom & Pan Support
- [ ] Color Coding für Warmth/Influence

### Phase 3: Alternative Views (45 Min)
- [ ] Grid View Component
- [ ] Network View (optional)
- [ ] Mobile-optimierte Ansicht
- [ ] Export Funktionen

### Phase 4: Intelligence Layer (30 Min)
- [ ] Auto-Detection von Hierarchien
- [ ] Title-based Inference
- [ ] Stakeholder Mapping
- [ ] Decision Path Visualization

### Phase 5: Integration (30 Min)
- [ ] Integration mit Contact Management
- [ ] Warmth Score Anzeige
- [ ] Communication History Links
- [ ] Dashboard Widgets

## 🔗 INTEGRATION POINTS

1. **Contact Management** - Hierarchie-Daten nutzen
2. **Relationship Warmth** - Farbkodierung
3. **Communication History** - Kontakt-Frequenz
4. **Smart Suggestions** - Basierend auf Hierarchie

## ⚠️ HÄUFIGE FEHLER VERMEIDEN

1. **Unklare Hierarchien**
   → Lösung: Intelligente Title-Inference

2. **Zu komplexe Darstellung**
   → Lösung: Multiple View-Modi

3. **Performance bei vielen Kontakten**
   → Lösung: Virtualisierung, Level-Filter

---

**Status:** BEREIT FÜR IMPLEMENTIERUNG  
**Geschätzte Zeit:** 195 Minuten  
**Nächstes Dokument:** [→ Social Media Integration](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/SOCIAL_MEDIA_INTEGRATION.md)  
**Parent:** [↑ Critical Success Factors](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/CRITICAL_SUCCESS_FACTORS.md)

**Hierarchien verstehen = Erfolgreicher verkaufen! 🏢✨**