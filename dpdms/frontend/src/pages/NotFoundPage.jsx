import { Link } from 'react-router-dom';

export default function NotFoundPage() {
  return (
    <div className="flex min-h-[60vh] flex-col items-center justify-center gap-3 text-center">
      <h1 className="text-3xl font-bold text-slate-900">404 - Not found</h1>
      <Link to="/" className="btn-primary">
        Back to start
      </Link>
    </div>
  );
}
