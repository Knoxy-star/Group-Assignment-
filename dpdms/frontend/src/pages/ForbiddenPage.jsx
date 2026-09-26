import { Link } from 'react-router-dom';

export default function ForbiddenPage() {
  return (
    <div className="flex min-h-[60vh] flex-col items-center justify-center gap-3 text-center">
      <h1 className="text-3xl font-bold text-slate-900">403 - Forbidden</h1>
      <p className="text-slate-500">Your role doesn't have access to this page.</p>
      <Link to="/" className="btn-primary">
        Back to start
      </Link>
    </div>
  );
}
