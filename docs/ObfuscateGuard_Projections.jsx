import { useState } from "react";
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer, AreaChart, Area, BarChart, Bar } from "recharts";

const data = [
  { month: "M6", conservative_mrr: 4500, base_mrr: 10200, aggressive_mrr: 22800, conservative_arr: 54000, base_arr: 122400, aggressive_arr: 273600, conservative_users: 300, base_users: 600, aggressive_users: 1200 },
  { month: "M12", conservative_mrr: 10200, base_mrr: 22800, aggressive_mrr: 66000, conservative_arr: 122400, base_arr: 273600, aggressive_arr: 792000, conservative_users: 600, base_users: 1200, aggressive_users: 3000 },
  { month: "M18", conservative_mrr: 19000, base_mrr: 46200, aggressive_mrr: 137500, conservative_arr: 228000, base_arr: 554400, aggressive_arr: 1650000, conservative_users: 1000, base_users: 2200, aggressive_users: 5500 },
  { month: "M24", conservative_mrr: 31500, base_mrr: 80500, aggressive_mrr: 252000, conservative_arr: 378000, base_arr: 966000, aggressive_arr: 3024000, conservative_users: 1500, base_users: 3500, aggressive_users: 9000 },
  { month: "M36", conservative_mrr: 69000, base_mrr: 162500, aggressive_mrr: 450000, conservative_arr: 828000, base_arr: 1950000, aggressive_arr: 5400000, conservative_users: 3000, base_users: 6500, aggressive_users: 15000 },
];

const formatEur = (v) => {
  if (v >= 1000000) return `€${(v / 1000000).toFixed(1)}M`;
  if (v >= 1000) return `€${(v / 1000).toFixed(0)}K`;
  return `€${v}`;
};

const CustomTooltip = ({ active, payload, label }) => {
  if (!active || !payload?.length) return null;
  return (
    <div className="bg-gray-900 border border-gray-700 rounded-lg p-3 shadow-xl">
      <p className="text-gray-300 font-semibold mb-2 text-sm">{label}</p>
      {payload.map((p, i) => (
        <p key={i} className="text-xs" style={{ color: p.color }}>
          {p.name}: <span className="font-bold">{typeof p.value === 'number' && p.value > 100 ? formatEur(p.value) : p.value?.toLocaleString()}</span>
        </p>
      ))}
    </div>
  );
};

export default function ObfuscateGuardProjections() {
  const [view, setView] = useState("arr");
  const [scenario, setScenario] = useState("all");

  const views = [
    { id: "arr", label: "ARR Projection" },
    { id: "mrr", label: "MRR Projection" },
    { id: "users", label: "Paying Users" },
  ];

  const scenarios = [
    { id: "all", label: "All Scenarios" },
    { id: "conservative", label: "Conservative", color: "#6366f1" },
    { id: "base", label: "Base", color: "#06b6d4" },
    { id: "aggressive", label: "Aggressive", color: "#10b981" },
  ];

  const getLines = () => {
    const prefix = view === "arr" ? "_arr" : view === "mrr" ? "_mrr" : "_users";
    const configs = [
      { key: `conservative${prefix}`, name: "Conservative", color: "#6366f1", dash: "5 5" },
      { key: `base${prefix}`, name: "Base", color: "#06b6d4" },
      { key: `aggressive${prefix}`, name: "Aggressive", color: "#10b981" },
    ];
    if (scenario !== "all") return configs.filter(c => c.key.startsWith(scenario));
    return configs;
  };

  const milestones = [
    { label: "v1.0 Free", month: "M6", color: "#f59e0b" },
    { label: "v2.0 Pro", month: "M12", color: "#8b5cf6" },
    { label: "v3.0 Biz", month: "M18", color: "#06b6d4" },
    { label: "Scale", month: "M24", color: "#10b981" },
  ];

  const competitorPricing = [
    { name: "ProGuard", price: 0, type: "One-time" },
    { name: "Our Plugin\n(Pro/yr)", price: 89, type: "Annual" },
    { name: "Our Plugin\n(Biz/yr)", price: 279, type: "Annual" },
    { name: "Zelix KM", price: 585, type: "One-time" },
    { name: "DashO", price: 3000, type: "Annual" },
  ];

  return (
    <div className="min-h-screen bg-gray-950 text-gray-100 p-4 md:p-6">
      <div className="max-w-6xl mx-auto">
        {/* Header */}
        <div className="mb-6">
          <div className="flex items-center gap-3 mb-1">
            <div className="w-8 h-8 rounded bg-indigo-600 flex items-center justify-center text-sm font-bold">OG</div>
            <h1 className="text-xl md:text-2xl font-bold text-white">ObfuscateGuard — Financial Projections</h1>
          </div>
          <p className="text-gray-500 text-sm ml-11">JetBrains Obfuscation Plugin | HTMETA.dev</p>
        </div>

        {/* KPI Cards */}
        <div className="grid grid-cols-2 md:grid-cols-4 gap-3 mb-6">
          {[
            { label: "TAM (2024)", value: "$1.25B", sub: "13.2% CAGR", accent: "text-emerald-400" },
            { label: "Target MRR (Y2)", value: "€80.5K", sub: "Base scenario", accent: "text-cyan-400" },
            { label: "Target ARR (Y3)", value: "€1.95M", sub: "Base scenario", accent: "text-indigo-400" },
            { label: "JB Developers", value: "20.8M", sub: "Global (2025)", accent: "text-amber-400" },
          ].map((kpi, i) => (
            <div key={i} className="bg-gray-900 border border-gray-800 rounded-lg p-3">
              <p className="text-gray-500 text-xs uppercase tracking-wide">{kpi.label}</p>
              <p className={`text-lg md:text-xl font-bold ${kpi.accent}`}>{kpi.value}</p>
              <p className="text-gray-600 text-xs">{kpi.sub}</p>
            </div>
          ))}
        </div>

        {/* View Toggle */}
        <div className="flex flex-wrap gap-2 mb-4">
          {views.map(v => (
            <button key={v.id} onClick={() => setView(v.id)}
              className={`px-3 py-1.5 rounded-md text-xs font-medium transition-all ${view === v.id ? 'bg-indigo-600 text-white' : 'bg-gray-800 text-gray-400 hover:text-white'}`}>
              {v.label}
            </button>
          ))}
          <div className="w-px bg-gray-700 mx-1" />
          {scenarios.map(s => (
            <button key={s.id} onClick={() => setScenario(s.id)}
              className={`px-3 py-1.5 rounded-md text-xs font-medium transition-all ${scenario === s.id ? 'bg-gray-700 text-white' : 'bg-gray-800 text-gray-400 hover:text-white'}`}>
              {s.id !== "all" && <span className="inline-block w-2 h-2 rounded-full mr-1.5" style={{ backgroundColor: s.color }} />}
              {s.label}
            </button>
          ))}
        </div>

        {/* Main Chart */}
        <div className="bg-gray-900 border border-gray-800 rounded-xl p-4 mb-6">
          <h2 className="text-sm font-semibold text-gray-400 mb-3">
            {view === "arr" ? "Annual Recurring Revenue" : view === "mrr" ? "Monthly Recurring Revenue" : "Paying Users"} — 36 Month Projection
          </h2>
          <ResponsiveContainer width="100%" height={340}>
            <AreaChart data={data}>
              <defs>
                {getLines().map(l => (
                  <linearGradient key={l.key} id={`grad_${l.key}`} x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor={l.color} stopOpacity={0.3} />
                    <stop offset="95%" stopColor={l.color} stopOpacity={0} />
                  </linearGradient>
                ))}
              </defs>
              <CartesianGrid strokeDasharray="3 3" stroke="#1f2937" />
              <XAxis dataKey="month" tick={{ fill: '#6b7280', fontSize: 12 }} />
              <YAxis tick={{ fill: '#6b7280', fontSize: 11 }} tickFormatter={view === "users" ? v => v.toLocaleString() : formatEur} />
              <Tooltip content={<CustomTooltip />} />
              <Legend wrapperStyle={{ fontSize: 12 }} />
              {getLines().map(l => (
                <Area key={l.key} type="monotone" dataKey={l.key} name={l.name}
                  stroke={l.color} strokeWidth={2} strokeDasharray={l.dash}
                  fill={`url(#grad_${l.key})`} dot={{ fill: l.color, r: 4 }} />
              ))}
            </AreaChart>
          </ResponsiveContainer>
        </div>

        {/* Bottom Row */}
        <div className="grid md:grid-cols-2 gap-4">
          {/* Pricing Comparison */}
          <div className="bg-gray-900 border border-gray-800 rounded-xl p-4">
            <h2 className="text-sm font-semibold text-gray-400 mb-3">Pricing vs Competitors (EUR)</h2>
            <ResponsiveContainer width="100%" height={220}>
              <BarChart data={competitorPricing} layout="vertical">
                <CartesianGrid strokeDasharray="3 3" stroke="#1f2937" />
                <XAxis type="number" tick={{ fill: '#6b7280', fontSize: 11 }} tickFormatter={v => `€${v}`} />
                <YAxis type="category" dataKey="name" tick={{ fill: '#9ca3af', fontSize: 10 }} width={80} />
                <Tooltip content={<CustomTooltip />} />
                <Bar dataKey="price" name="Price" radius={[0, 4, 4, 0]}
                  fill="#6366f1"
                  label={{ position: 'right', fill: '#9ca3af', fontSize: 11, formatter: v => `€${v}` }} />
              </BarChart>
            </ResponsiveContainer>
          </div>

          {/* Milestones */}
          <div className="bg-gray-900 border border-gray-800 rounded-xl p-4">
            <h2 className="text-sm font-semibold text-gray-400 mb-3">Implementation Milestones</h2>
            <div className="space-y-3">
              {[
                { phase: "Phase 0", weeks: "W1–4", title: "Foundation", desc: "Core architecture, CI/CD, plugin signing", color: "border-gray-600" },
                { phase: "Phase 1", weeks: "W5–10", title: "Free Tier MVP", desc: "Name obfuscation, ProGuard import, IDE UI", color: "border-amber-500" },
                { phase: "Phase 2", weeks: "W11–18", title: "Pro Tier", desc: "String encryption, flow obfuscation, preview", color: "border-purple-500" },
                { phase: "Phase 3", weeks: "W19–26", title: "Business Tier", desc: "Anti-debug, tamper detection, CI/CD export", color: "border-cyan-500" },
                { phase: "Phase 4", weeks: "W27–36", title: "Enterprise & Growth", desc: "Custom API, localization, partnerships", color: "border-emerald-500" },
              ].map((m, i) => (
                <div key={i} className={`border-l-2 ${m.color} pl-3 py-1`}>
                  <div className="flex items-center gap-2">
                    <span className="text-xs font-mono text-gray-500">{m.weeks}</span>
                    <span className="text-sm font-semibold text-white">{m.title}</span>
                    <span className="text-xs text-gray-600">{m.phase}</span>
                  </div>
                  <p className="text-xs text-gray-500">{m.desc}</p>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* Market size footer */}
        <div className="mt-4 bg-gray-900 border border-gray-800 rounded-xl p-4">
          <h2 className="text-sm font-semibold text-gray-400 mb-2">Market Opportunity Summary</h2>
          <div className="grid grid-cols-3 gap-4 text-center">
            <div>
              <p className="text-xs text-gray-500 uppercase">Code Obfuscation TAM</p>
              <p className="text-lg font-bold text-emerald-400">$1.25B → $3.7B</p>
              <p className="text-xs text-gray-600">2024 → 2033 (13.2% CAGR)</p>
            </div>
            <div>
              <p className="text-xs text-gray-500 uppercase">JetBrains Plugin Gap</p>
              <p className="text-lg font-bold text-amber-400">0 Competitors</p>
              <p className="text-xs text-gray-600">No modern obfuscation plugin exists</p>
            </div>
            <div>
              <p className="text-xs text-gray-500 uppercase">Price Advantage</p>
              <p className="text-lg font-bold text-cyan-400">10x Cheaper</p>
              <p className="text-xs text-gray-600">vs DashO ($3K/yr) with better UX</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
