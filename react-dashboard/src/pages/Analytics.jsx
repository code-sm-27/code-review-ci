import { useQuery } from '@tanstack/react-query';
import { api } from '../lib/api';
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, PieChart, Pie, Cell, LineChart, Line } from 'recharts';

const COLORS = ['#ef4444', '#f59e0b', '#10b981', '#3b82f6'];

export const Analytics = () => {
  const { data: severityData } = useQuery({
    queryKey: ['analytics-severity'],
    queryFn: async () => {
      try {
        const res = await api.get('/analytics/severity');
        if (!res.data || typeof res.data !== 'object') return [];
        return Object.entries(res.data).map(([name, value]) => ({ name, value }));
      } catch (err) {
        return [];
      }
    },
    initialData: []
  });

  const { data: trendData } = useQuery({
    queryKey: ['analytics-trend'],
    queryFn: async () => {
      try {
        const res = await api.get('/analytics/trend');
        if (!res.data || typeof res.data !== 'object') return [];
        return Object.entries(res.data).map(([date, count]) => ({ date, count }));
      } catch (err) {
        return [];
      }
    },
    initialData: []
  });

  return (
    <div className="animate-fade-in">
      <header className="page-header">
        <h1>Analytics</h1>
        <p>Insights across all automated code reviews</p>
      </header>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '24px' }}>
        <div className="glass-card" style={{ padding: '24px', minWidth: 0 }}>
          <h3 style={{ marginTop: 0 }}>Review Severity Distribution</h3>
          <div style={{ height: '300px', width: '100%', display: 'flex', justifyContent: 'center' }}>
            <PieChart width={300} height={300}>
              <Pie data={severityData?.length ? severityData : [{ name: 'No Data', value: 1 }]} innerRadius={60} outerRadius={80} paddingAngle={5} dataKey="value">
                {(severityData?.length ? severityData : [{ name: 'No Data', value: 1 }]).map((_, index) => (
                  <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                ))}
              </Pie>
              <Tooltip contentStyle={{ background: '#1e293b', border: 'none', borderRadius: '8px', color: '#fff' }} />
            </PieChart>
          </div>
        </div>

        <div className="glass-card" style={{ padding: '24px', minWidth: 0 }}>
          <h3 style={{ marginTop: 0 }}>Reviews Over Time</h3>
          <div style={{ height: '300px', width: '100%' }}>
            <LineChart width={400} height={300} data={trendData?.length ? trendData : [{ date: 'Today', count: 0 }]}>
              <XAxis dataKey="date" stroke="#94a3b8" />
              <YAxis stroke="#94a3b8" />
              <Tooltip contentStyle={{ background: '#1e293b', border: 'none', borderRadius: '8px', color: '#fff' }} />
              <Line type="monotone" dataKey="count" stroke="#3b82f6" strokeWidth={3} dot={{ r: 4 }} />
            </LineChart>
          </div>
        </div>
      </div>
    </div>
  );
};
