import { useEffect } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { GitBranch as Github } from 'lucide-react';

export const Login = () => {
  const [searchParams] = useSearchParams();
  const { login, token } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    const accessToken = searchParams.get('accessToken');
    const refreshToken = searchParams.get('refreshToken');
    if (accessToken && refreshToken) {
      login(accessToken, refreshToken);
      navigate('/');
    } else if (token) {
      navigate('/');
    }
  }, [searchParams, login, navigate, token]);

  const handleGithubLogin = () => {
    const baseUrl = import.meta.env.VITE_API_URL ? import.meta.env.VITE_API_URL.replace('/api/v1', '') : 'http://localhost:8080';
    window.location.href = `${baseUrl}/oauth2/authorization/github`;
  };

  return (
    <div className="min-h-screen bg-slate-950 flex flex-col items-center justify-center relative overflow-hidden text-slate-100">
      {/* Dynamic Background Elements */}
      <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[800px] h-[800px] bg-blue-600/10 rounded-full blur-[100px] pointer-events-none"></div>
      <div className="absolute bottom-0 right-0 w-[600px] h-[600px] bg-indigo-600/10 rounded-full blur-[100px] pointer-events-none"></div>
      
      <div className="glass-card p-10 md:p-14 w-full max-w-md mx-4 relative z-10 flex flex-col items-center animate-fade-in border border-slate-800/60 shadow-[0_20px_60px_rgba(0,0,0,0.5)]">
        <div className="w-20 h-20 bg-gradient-to-br from-blue-500 to-indigo-600 rounded-2xl flex items-center justify-center mb-8 shadow-lg shadow-blue-500/30 transform rotate-3 hover:rotate-0 transition-transform duration-300">
          <Github size={40} className="text-white" />
        </div>
        
        <h1 className="text-3xl font-bold mb-3 text-center bg-clip-text text-transparent bg-gradient-to-r from-white to-slate-400">
          CodeReview CI
        </h1>
        
        <p className="text-slate-400 text-center mb-10 leading-relaxed">
          Supercharge your workflow with automated, AI-powered code reviews for your Pull Requests.
        </p>
        
        <button 
          onClick={handleGithubLogin} 
          className="w-full btn btn-primary py-3.5 text-lg shadow-blue-500/25 flex justify-center items-center gap-3 relative overflow-hidden group"
        >
          <div className="absolute inset-0 w-full h-full bg-white/20 scale-x-0 group-hover:scale-x-100 origin-left transition-transform duration-300"></div>
          <Github size={22} className="relative z-10" />
          <span className="relative z-10 font-semibold tracking-wide">Continue with GitHub</span>
        </button>
        
        <p className="mt-8 text-xs text-slate-500 text-center max-w-[250px]">
          By continuing, you agree to our Terms of Service and Privacy Policy.
        </p>
      </div>
    </div>
  );
};
