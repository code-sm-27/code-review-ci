import { useEffect } from 'react';
import { useQueryClient } from '@tanstack/react-query';

export function useRealTimeReviews() {
  const queryClient = useQueryClient();

  useEffect(() => {
    // We only want to open the connection once we have a valid token (handled by layout/auth typically)
    // Here we're assuming the token is stored in localStorage or sent automatically via cookies.
    // Given the architecture, let's just connect to the SSE endpoint directly.
    // Note: If using JWT in Auth header, standard EventSource doesn't support custom headers easily.
    // If standard EventSource doesn't work with JWT, we might need to pass token in URL params.
    // For now, let's assume standard auth cookies or we will pass token in URL if needed.
    
    const token = localStorage.getItem('jwt');
    const baseUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080/api/v1';
    
    // Ensure baseUrl doesn't have a trailing slash for consistency
    const cleanBaseUrl = baseUrl.endsWith('/') ? baseUrl.slice(0, -1) : baseUrl;
    const sseUrl = token ? `${cleanBaseUrl}/sse/subscribe?token=${token}` : `${cleanBaseUrl}/sse/subscribe`;
    
    // Using native EventSource
    const eventSource = new EventSource(sseUrl);

    eventSource.onopen = () => {
      console.log('SSE connection opened.');
    };

    eventSource.addEventListener('review_ready', (event) => {
      try {
        const data = JSON.parse(event.data);
        console.log('Real-time review notification received:', data);
        
        // Invalidate the 'reviews' query cache so React Query re-fetches the latest reviews automatically
        queryClient.invalidateQueries({ queryKey: ['reviews'] });
        
        // Optionally, we could dispatch a custom event here for toast notifications
        window.dispatchEvent(new CustomEvent('new-review-toast', { detail: data }));

      } catch (error) {
        console.error('Error parsing SSE event data:', error);
      }
    });

    eventSource.addEventListener('INIT', (event) => {
      console.log('SSE Init:', event.data);
    });

    eventSource.onerror = (error) => {
      console.error('SSE Error:', error);
      eventSource.close(); // Close to avoid infinite retry loops if unauthorized
    };

    return () => {
      eventSource.close();
      console.log('SSE connection closed.');
    };
  }, [queryClient]);
}
