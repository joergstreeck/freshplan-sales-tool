import React, { useState } from 'react';
import { Paper, Box, TextField, MenuItem, Button, Typography, Alert } from '@mui/material';
import type { ROICalcRequest } from '../../types/cockpit';
import { useROI } from '../../hooks/useROI';

export default function ROICalculator() {
  const [form, setForm] = useState<ROICalcRequest>({
    mealsPerDay: 120, daysPerMonth: 26, laborMinutesSavedPerMeal: 2.5,
    laborCostPerHour: 22, foodCostPerMeal: 3.2, wasteReductionPct: 8, channel: 'DIRECT', investment: 15000
  });
  const { loading, error, result, calculate } = useROI();

  const onChange = (k: keyof ROICalcRequest, v: any) => setForm({ ...form, [k]: v });

  return (
    <Paper elevation={0} sx={{ p: 2 }}>
      <Typography variant="h6" sx={{ mb: 2 }}>ROI-Kalkulator</Typography>
      <Box sx={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 2 }}>
        <TextField label="Meals/Tag" type="number" value={form.mealsPerDay} onChange={e=>onChange('mealsPerDay', Number(e.target.value))}/>
        <TextField label="Tage/Monat" type="number" value={form.daysPerMonth} onChange={e=>onChange('daysPerMonth', Number(e.target.value))}/>
        <TextField label="Minutenersparnis/Meal" type="number" value={form.laborMinutesSavedPerMeal} onChange={e=>onChange('laborMinutesSavedPerMeal', Number(e.target.value))}/>
        <TextField label="Lohn €/Stunde" type="number" value={form.laborCostPerHour} onChange={e=>onChange('laborCostPerHour', Number(e.target.value))}/>
        <TextField label="Food-Kosten €/Meal" type="number" value={form.foodCostPerMeal} onChange={e=>onChange('foodCostPerMeal', Number(e.target.value))}/>
        <TextField label="Waste-Reduktion %" type="number" value={form.wasteReductionPct} onChange={e=>onChange('wasteReductionPct', Number(e.target.value))}/>
        <TextField label="Channel" select value={form.channel} onChange={e=>onChange('channel', e.target.value)}>
          <MenuItem value="DIRECT">DIRECT</MenuItem>
          <MenuItem value="PARTNER">PARTNER</MenuItem>
        </TextField>
        <TextField label="Investitionskosten (Capex) €" type="number" value={form.investment||0} onChange={e=>onChange('investment', Number(e.target.value))}/>
      </Box>
      <Box sx={{ mt: 2, display: 'flex', gap: 2 }}>
        <Button variant="contained" onClick={()=>calculate(form)} disabled={loading}>Berechnen</Button>
      </Box>
      {error && <Alert severity="error" sx={{ mt: 2 }}>{error}</Alert>}
      {result && <Box sx={{ mt: 2 }}>
        <Typography>Arbeitskosten-Ersparnis/Monat: <b>{result.monthlyLaborSavings.toFixed(2)} €</b></Typography>
        <Typography>Waste-Ersparnis/Monat: <b>{result.monthlyWasteSavings.toFixed(2)} €</b></Typography>
        <Typography>Gesamt/Monat: <b>{result.totalMonthlySavings.toFixed(2)} €</b></Typography>
        {typeof result.paybackMonths === 'number' && <Typography>Amortisation: <b>{result.paybackMonths.toFixed(1)} Monate</b></Typography>}
      </Box>}
    </Paper>
  );
}
