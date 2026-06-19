import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { api } from '../lib/api';
import { GitBranch as Github, ExternalLink, Plus, Search } from 'lucide-react';

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
      const res = await api.post('/repos/connect', newRepo);
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
    <div className="flex flex-col gap-8 animate-fade-in w-full max-w-5xl mx-auto">
      <header className="flex flex-col gap-2">
        <h1 className="text-3xl md:text-4xl font-bold bg-clip-text text-transparent bg-gradient-to-r from-white to-slate-400">
          Repositories
        </h1>
        <p className="text-slate-400 text-lg">Manage your connected GitHub repositories</p>
      </header>

      <div className="glass-card p-6 md:p-8 flex flex-col gap-6 relative overflow-hidden">
        {/* Decorative background element */}
        <div className="absolute top-0 right-0 -mt-10 -mr-10 w-40 h-40 bg-blue-500/10 rounded-full blur-3xl pointer-events-none"></div>
        
        <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 pb-4 border-b border-slate-700/50 relative z-10">
          <h3 className="text-xl font-bold text-slate-100 flex items-center gap-2">
            Connected Repositories
            <span className="bg-slate-800 text-slate-300 text-xs py-1 px-2 rounded-full font-medium border border-slate-700">
              {repos?.length || 0}
            </span>
          </h3>
          <button 
            className={`btn ${isAdding ? 'bg-slate-700 hover:bg-slate-600 text-white' : 'btn-primary shadow-blue-500/20'}`} 
            onClick={() => setIsAdding(!isAdding)}
          >
            {isAdding ? 'Cancel' : <><Plus size={18} /> Connect Repo</>}
          </button>
        </div>

        {isAdding && (
          <form onSubmit={handleSubmit} className="bg-slate-800/50 border border-blue-500/30 p-6 rounded-xl relative z-10 animate-fade-in">
            <h4 className="text-lg font-semibold text-slate-200 mb-4">Add a new repository</h4>
            <div className="grid gap-4 mb-6">
              <div>
                <label className="block text-sm font-medium text-slate-400 mb-1">Full Name</label>
                <input 
                  type="text" 
                  placeholder="e.g., octocat/Hello-World" 
                  className="w-full bg-slate-900/50 border border-slate-700 rounded-lg px-4 py-2.5 text-slate-200 focus:outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500 transition-all placeholder-slate-600" 
                  value={formData.fullName}
                  onChange={e => setFormData({...formData, fullName: e.target.value})}
                  required 
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-slate-400 mb-1">GitHub URL</label>
                <input 
                  type="url" 
                  placeholder="e.g., https://github.com/octocat/Hello-World" 
                  className="w-full bg-slate-900/50 border border-slate-700 rounded-lg px-4 py-2.5 text-slate-200 focus:outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500 transition-all placeholder-slate-600" 
                  value={formData.url}
                  onChange={e => setFormData({...formData, url: e.target.value})}
                  required 
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-slate-400 mb-1">Description (Optional)</label>
                <input 
                  type="text" 
                  placeholder="What is this repository for?" 
                  className="w-full bg-slate-900/50 border border-slate-700 rounded-lg px-4 py-2.5 text-slate-200 focus:outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500 transition-all placeholder-slate-600" 
                  value={formData.description}
                  onChange={e => setFormData({...formData, description: e.target.value})}
                />
              </div>
            </div>
            <button type="submit" className="btn btn-primary w-full sm:w-auto" disabled={connectMutation.isPending}>
              {connectMutation.isPending ? 'Connecting...' : 'Save Repository'}
            </button>
          </form>
        )}

        {isLoading ? (
          <div className="flex justify-center items-center py-12">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-500"></div>
          </div>
        ) : (
          <div className="grid gap-4 relative z-10">
            {repos?.map((repo) => (
              <div key={repo.id} className="group bg-slate-800/30 hover:bg-slate-800/60 p-5 rounded-xl border border-slate-700/50 hover:border-slate-600 transition-all duration-300 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                <div className="flex items-start gap-4">
                  <div className="w-12 h-12 rounded-full bg-slate-700/50 flex items-center justify-center text-slate-400 group-hover:text-blue-400 group-hover:bg-blue-500/10 transition-colors shrink-0">
                    <Github size={24} />
                  </div>
                  <div>
                    <h4 className="text-lg font-semibold text-slate-200 group-hover:text-blue-400 transition-colors mb-1">{repo.fullName}</h4>
                    <p className="text-slate-400 text-sm">{repo.description || 'No description provided'}</p>
                  </div>
                </div>
                <div className="flex items-center gap-3 sm:w-auto w-full">
                  <a href={repo.url} target="_blank" rel="noreferrer" className="flex items-center justify-center p-2.5 rounded-lg bg-slate-700/50 hover:bg-slate-600 text-slate-300 transition-colors" title="View on GitHub">
                    <Github size={18} />
                  </a>
                  <Link to={`/repos/${repo.id}`} className="flex-1 sm:flex-none flex items-center justify-center gap-2 px-4 py-2.5 rounded-lg bg-blue-500/10 hover:bg-blue-500/20 text-blue-400 hover:text-blue-300 transition-colors font-medium border border-blue-500/20">
                    <ExternalLink size={16} />
                    <span>View Details</span>
                  </Link>
                </div>
              </div>
            ))}
            {repos?.length === 0 && (
              <div className="text-center py-16 px-4 bg-slate-800/20 rounded-xl border border-dashed border-slate-700/50">
                <div className="w-16 h-16 bg-slate-800 rounded-full flex items-center justify-center mx-auto mb-4">
                  <Search size={28} className="text-slate-500" />
                </div>
                <p className="text-slate-300 text-lg font-medium mb-1">No repositories found</p>
                <p className="text-slate-500 mb-6">You haven't connected any repositories yet.</p>
                <button className="btn btn-primary shadow-blue-500/20" onClick={() => setIsAdding(true)}>
                  <Plus size={18} /> Connect Your First Repo
                </button>
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
};
