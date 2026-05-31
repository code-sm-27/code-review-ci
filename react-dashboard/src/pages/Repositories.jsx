import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { api } from '../lib/api';
import { GitBranch as Github, ExternalLink, Plus } from 'lucide-react';
import './Dashboard.css'; // Reusing dashboard header styles

export const Repositories = () => {
  const queryClient = useQueryClient();
  const [isAdding, setIsAdding] = useState(false);
  const [formData, setFormData] = useState({ fullName: '', description: '', url: '' });

  const { data: repos, isLoading } = useQuery({
    queryKey: ['repos'],
    queryFn: async () => {
      const res = await api.get('/repos');
      return res.data;
    }
  });

  const connectMutation = useMutation({
    mutationFn: async (newRepo) => {
      const res = await api.post('/repos', newRepo);
      return res.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['repos'] });
      setIsAdding(false);
      setFormData({ fullName: '', description: '', url: '' });
    },
    onError: (err) => {
      alert(err.response?.data?.message || 'Failed to connect repository');
    }
  });

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!formData.fullName || !formData.url) return;
    connectMutation.mutate(formData);
  };

  return (
    <div className="animate-fade-in">
      <header className="page-header">
        <h1>Repositories</h1>
        <p>Manage your connected GitHub repositories</p>
      </header>

      <div className="glass-card table-card">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
          <h3>Connected Repositories</h3>
          <button className="btn btn-primary" onClick={() => setIsAdding(!isAdding)}>
            {isAdding ? 'Cancel' : <><Plus size={18} /> Connect Repo</>}
          </button>
        </div>

        {isAdding && (
          <form onSubmit={handleSubmit} className="glass-card" style={{ padding: '20px', marginBottom: '24px', background: 'rgba(255,255,255,0.05)' }}>
            <h4 style={{ marginTop: 0, marginBottom: '16px' }}>Add a new repository</h4>
            <div style={{ display: 'grid', gap: '12px', marginBottom: '16px' }}>
              <input 
                type="text" 
                placeholder="Full Name (e.g., octocat/Hello-World)" 
                className="input" 
                value={formData.fullName}
                onChange={e => setFormData({...formData, fullName: e.target.value})}
                required 
              />
              <input 
                type="url" 
                placeholder="GitHub URL (e.g., https://github.com/octocat/Hello-World)" 
                className="input" 
                value={formData.url}
                onChange={e => setFormData({...formData, url: e.target.value})}
                required 
              />
              <input 
                type="text" 
                placeholder="Description (Optional)" 
                className="input" 
                value={formData.description}
                onChange={e => setFormData({...formData, description: e.target.value})}
              />
            </div>
            <button type="submit" className="btn btn-primary" disabled={connectMutation.isPending}>
              {connectMutation.isPending ? 'Connecting...' : 'Save Repository'}
            </button>
          </form>
        )}

        {isLoading ? (
          <p>Loading repositories...</p>
        ) : (
          <div style={{ display: 'grid', gap: '16px' }}>
            {repos?.map((repo) => (
              <div key={repo.id} className="glass-card" style={{ padding: '16px', display: 'flex', justifyContent: 'space-between', alignItems: 'center', background: 'rgba(255,255,255,0.02)' }}>
                <div>
                  <h4 style={{ margin: '0 0 4px 0', fontSize: '1.1rem' }}>{repo.fullName}</h4>
                  <p className="text-secondary" style={{ margin: 0, fontSize: '0.9rem' }}>{repo.description || 'No description provided'}</p>
                </div>
                <a href={repo.url} target="_blank" rel="noreferrer" className="btn" style={{ background: 'rgba(255,255,255,0.1)' }}>
                  <ExternalLink size={16} />
                  View
                </a>
              </div>
            ))}
            {repos?.length === 0 && (
              <p className="text-secondary text-center">No repositories connected yet.</p>
            )}
          </div>
        )}
      </div>
    </div>
  );
};
