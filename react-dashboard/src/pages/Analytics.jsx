import { useQuery } from '@tanstack/react-query';
import { api } from '../lib/api';
import { XAxis, YAxis, Tooltip, ResponsiveContainer, PieChart, Pie, Cell, LineChart, Line } from 'recharts';
import { BarChart2 } from 'lucide-react';

const COLORS = ['#f87171', '#fbbf24', '#60a5fa', '#34d399']; // Red, Amber, Blue, Emerald

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

  const renderCustomTooltip = ({ active, payload }) => {
    if (active && payload && payload.length) {
      return (
        <div className="bg-slate-900/90 border border-slate-700/50 p-3 rounded-lg shadow-xl backdrop-blur-md">
          <p className="text-slate-200 font-medium">{`${payload[0].name || payload[0].payload.date} : ${payload[0].value}`}</p>
        </div>
      );
    }
    return null;
  };

  return (
    <div className="flex flex-col gap-8 animate-fade-in w-full max-w-6xl mx-auto">
      <header className="flex flex-col gap-2">
        <h1 className="text-3xl md:text-4xl font-bold bg-clip-text text-transparent bg-gradient-to-r from-white to-slate-400 flex items-center gap-3">
          <BarChart2 className="text-blue-500" size={32} />
          Analytics
        </h1>
        <p className="text-slate-400 text-lg">Insights across all automated code reviews</p>
      </header>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6 md:gap-8">
        <div className="glass-card p-6 md:p-8 flex flex-col items-center">
          <h3 className="text-xl font-bold text-slate-100 mb-6 self-start w-full border-b border-slate-700/50 pb-4">
            Review Severity Distribution
          </h3>
          <div className="h-72 w-full flex justify-center items-center relative">
            {severityData?.length === 0 && (
              <div className="absolute inset-0 flex items-center justify-center pointer-events-none">
                <p className="text-slate-500">No data available</p>
              </div>
            )}
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie 
                  data={severityData?.length ? severityData : [{ name: 'No Data', value: 1 }]} 
                  innerRadius={70} 
                  outerRadius={100} 
                  paddingAngle={5} 
                  dataKey="value"
                  stroke="rgba(0,0,0,0)"
                >
                  {(severityData?.length ? severityData : [{ name: 'No Data', value: 1 }]).map((_, index) => (
                    <Cell key={`cell-${index}`} fill={severityData?.length ? COLORS[index % COLORS.length] : '#334155'} />
                  ))}
                </Pie>
                <Tooltip content={renderCustomTooltip} />
              </PieChart>
            </ResponsiveContainer>
          </div>
          
          {/* Custom Legend */}
          {severityData?.length > 0 && (
            <div className="flex flex-wrap justify-center gap-4 mt-4">
              {severityData.map((entry, index) => (
                <div key={`legend-${index}`} className="flex items-center gap-2">
                  <div className="w-3 h-3 rounded-full" style={{ backgroundColor: COLORS[index % COLORS.length] }}></div>
                  <span className="text-slate-300 text-sm font-medium">{entry.name}</span>
                </div>
              ))}
            </div>
          )}
        </div>

        <div className="glass-card p-6 md:p-8 flex flex-col">
          <h3 className="text-xl font-bold text-slate-100 mb-6 self-start w-full border-b border-slate-700/50 pb-4">
            Reviews Over Time
          </h3>
          <div className="h-72 w-full mt-auto relative">
            {trendData?.length === 0 && (
              <div className="absolute inset-0 flex items-center justify-center pointer-events-none z-10">
                <p className="text-slate-500">No data available</p>
              </div>
            )}
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={trendData?.length ? trendData : [{ date: 'Today', count: 0 }]}>
                <XAxis dataKey="date" stroke="#64748b" tick={{ fill: '#64748b' }} axisLine={{ stroke: '#334155' }} tickLine={false} dy={10} />
                <YAxis stroke="#64748b" tick={{ fill: '#64748b' }} axisLine={{ stroke: '#334155' }} tickLine={false} dx={-10} allowDecimals={false} />
                <Tooltip content={renderCustomTooltip} />
                <Line 
                  type="monotone" 
                  dataKey="count" 
                  stroke="#3b82f6" 
                  strokeWidth={3} 
                  dot={trendData?.length ? { r: 4, fill: '#1e293b', strokeWidth: 2 } : false} 
                  activeDot={trendData?.length ? { r: 6, fill: '#3b82f6', stroke: '#fff' } : false} 
                  animationDuration={1500} 
                />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </div>
      </div>
    </div>
  );
};
