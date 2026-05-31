import { useEffect } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { GitBranch as Github } from 'lucide-react';
import './Login.css';

export const Login = () => {
  const [searchParams] = useSearchParams();
  const { login, token } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    const urlToken = searchParams.get('token');
    if (urlToken) {
      login(urlToken);
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
    <div className="login-container">
      <div className="login-card glass-card animate-fade-in">
        <div className="login-header">
          <Github size={48} className="login-icon" />
          <h1>CodeReview CI</h1>
          <p>Automated AI Code Reviews for your Pull Requests</p>
        </div>
        
        <button onClick={handleGithubLogin} className="btn btn-primary login-btn">
          <Github size={20} />
          Continue with GitHub
        </button>
      </div>
    </div>
  );
};
