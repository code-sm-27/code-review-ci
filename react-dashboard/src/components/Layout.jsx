import { Outlet, Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { GitBranch as Github, LogOut, LayoutDashboard, FileCode2, BarChart2, Bell } from 'lucide-react';
import { useState, useEffect } from 'react';
import { useRealTimeReviews } from '../hooks/useRealTimeReviews';

export const Layout = () => {
  const { logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [toast, setToast] = useState(null);

  useRealTimeReviews();

  useEffect(() => {
    const handleNewReview = (event) => {
      const data = event.detail;
      setToast(`Review completed for PR #${data.prNumber} in ${data.repoFullName}`);
      setTimeout(() => setToast(null), 5000);
    };

    window.addEventListener('new-review-toast', handleNewReview);
    return () => window.removeEventListener('new-review-toast', handleNewReview);
  }, []);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const navItems = [
    { path: '/', icon: LayoutDashboard, label: 'Dashboard' },
    { path: '/repos', icon: FileCode2, label: 'Repositories' },
    { path: '/analytics', icon: BarChart2, label: 'Analytics' },
  ];

  return (
    <div className="flex min-h-screen bg-slate-900 text-slate-100 font-sans">
      <aside className="w-64 flex flex-col p-6 glass-card rounded-none border-t-0 border-b-0 border-l-0 shadow-xl border-r border-slate-700/50 bg-slate-800/40 relative z-10">
        <div className="flex items-center gap-3 mb-10">
          <Github size={28} className="text-blue-500 drop-shadow-[0_0_8px_rgba(59,130,246,0.5)]" />
          <h2 className="text-xl font-bold bg-clip-text text-transparent bg-gradient-to-r from-white to-slate-400">
            CodeReview CI
          </h2>
        </div>
        
        <nav className="flex flex-col gap-2 flex-grow">
          {navItems.map((item) => {
            const Icon = item.icon;
            const isActive = location.pathname === item.path || (item.path !== '/' && location.pathname.startsWith(item.path));
            return (
              <Link 
                key={item.path} 
                to={item.path} 
                className={`flex items-center gap-3 px-4 py-3 rounded-xl font-medium transition-all duration-300 group
                  ${isActive 
                    ? 'bg-blue-500/10 text-blue-400 shadow-[inset_0_0_20px_rgba(59,130,246,0.1)] translate-x-1' 
                    : 'text-slate-400 hover:bg-white/5 hover:text-slate-200 hover:translate-x-1'
                  }`}
              >
                <Icon size={20} className={`transition-transform duration-300 ${isActive ? 'scale-110' : 'group-hover:scale-110'}`} />
                <span>{item.label}</span>
              </Link>
            )
          })}
        </nav>
        
        <div className="mt-auto pt-6 border-t border-slate-700/50">
          <button 
            onClick={handleLogout} 
            className="w-full flex items-center justify-center gap-2 px-4 py-3 rounded-xl text-slate-400 font-medium transition-all duration-300 hover:bg-red-500/10 hover:text-red-400 border border-transparent hover:border-red-500/30"
          >
            <LogOut size={18} />
            <span>Logout</span>
          </button>
        </div>
      </aside>

      <main className="flex-1 flex flex-col min-h-screen relative overflow-y-auto">
        <div className="flex-1 p-8 md:p-10 max-w-7xl mx-auto w-full animate-fade-in">
          <Outlet />
        </div>
        <footer className="text-center p-6 text-slate-500 text-sm mt-auto border-t border-slate-800/50">
          &copy; {new Date().getFullYear()} CodeReview CI. Engineered for excellence.
        </footer>
      </main>
      
      {toast && (
        <div className="fixed bottom-6 right-6 flex items-center gap-3 px-6 py-4 glass-card border-l-4 border-l-blue-500 text-slate-100 font-medium shadow-[0_10px_40px_rgba(0,0,0,0.3)] z-50 animate-fade-in rounded-xl">
          <Bell size={20} className="text-blue-400" />
          <span>{toast}</span>
        </div>
      )}
    </div>
  );
};
