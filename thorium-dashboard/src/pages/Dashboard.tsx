import React, { useState, useEffect } from 'react';
import {
  Key,
  Package,
  Users,
  TrendingUp,
  Activity,
  AlertTriangle,
  CheckCircle,
  XCircle,
  Plus,
  Download,
  RefreshCw
} from 'lucide-react';

// Mock data for demonstration
interface Stats {
  totalLicenses: number;
  activeLicenses: number;
  expiredLicenses: number;
  totalProducts: number;
}

interface Activity {
  id: number;
  action: string;
  details: string;
  timestamp: string;
}

const Dashboard: React.FC = () => {
  const [stats, setStats] = useState<Stats>({
    totalLicenses: 156,
    activeLicenses: 124,
    expiredLicenses: 32,
    totalProducts: 5
  });

  const [recentActivity] = useState<Activity[]>([
    { id: 1, action: 'License Generated', details: 'New license for Machine ABC123', timestamp: '2 mins ago' },
    { id: 2, action: 'Product Created', details: 'New product "Inventory Pro" added', timestamp: '1 hour ago' },
    { id: 3, action: 'License Validated', details: 'License checked for Machine XYZ789', timestamp: '2 hours ago' },
    { id: 4, action: 'Payment Received', details: '$49.99 payment via Stripe', timestamp: '3 hours ago' },
    { id: 5, action: 'License Expired', details: 'License for Machine DEF456 expired', timestamp: '5 hours ago' },
  ]);

  const [loading, setLoading] = useState(false);

  const handleRefresh = () => {
    setLoading(true);
    setTimeout(() => setLoading(false), 1000);
  };

  const StatCard: React.FC<{
    title: string;
    value: number;
    icon: React.ReactNode;
    color: string;
    change?: string;
  }> = ({ title, value, icon, color, change }) => (
    <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-100 hover:shadow-md transition-shadow">
      <div className="flex items-center justify-between">
        <div>
          <p className="text-sm text-gray-500 font-medium">{title}</p>
          <p className="text-3xl font-bold mt-2" style={{ color }}>{value}</p>
          {change && (
            <p className="text-sm text-green-600 mt-1 flex items-center">
              <TrendingUp className="w-4 h-4 mr-1" />
              {change}
            </p>
          )}
        </div>
        <div className={`p-3 rounded-lg ${color} bg-opacity-10`} style={{ backgroundColor: `${color}20` }}>
          <div style={{ color }}>{icon}</div>
        </div>
      </div>
    </div>
  );

  const getStatusIcon = (action: string) => {
    if (action.includes('Generated') || action.includes('Created')) {
      return <Plus className="w-4 h-4 text-green-600" />;
    }
    if (action.includes('Validated') || action.includes('Checked')) {
      return <CheckCircle className="w-4 h-4 text-blue-600" />;
    }
    if (action.includes('Expired')) {
      return <XCircle className="w-4 h-4 text-red-600" />;
    }
    if (action.includes('Payment')) {
      return <Activity className="w-4 h-4 text-purple-600" />;
    }
    return <Activity className="w-4 h-4 text-gray-600" />;
  };

  return (
    <div className="p-6 bg-gray-50 min-h-screen">
      {/* Header */}
      <div className="flex items-center justify-between mb-8">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Dashboard</h1>
          <p className="text-gray-500 mt-1">Welcome back! Here's your licensing overview.</p>
        </div>
        <div className="flex items-center gap-3">
          <button
            onClick={handleRefresh}
            className="flex items-center gap-2 px-4 py-2 bg-white border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors"
          >
            <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
            Refresh
          </button>
          <button className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors">
            <Plus className="w-4 h-4" />
            Generate Key
          </button>
        </div>
      </div>

      {/* Stats Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
        <StatCard
          title="Total Licenses"
          value={stats.totalLicenses}
          icon={<Key className="w-6 h-6" />}
          color="#3B82F6"
          change="+12%"
        />
        <StatCard
          title="Active Licenses"
          value={stats.activeLicenses}
          icon={<CheckCircle className="w-6 h-6" />}
          color="#10B981"
        />
        <StatCard
          title="Expired Licenses"
          value={stats.expiredLicenses}
          icon={<XCircle className="w-6 h-6" />}
          color="#EF4444"
        />
        <StatCard
          title="Products"
          value={stats.totalProducts}
          icon={<Package className="w-6 h-6" />}
          color="#8B5CF6"
        />
      </div>

      {/* Main Content Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Recent Activity */}
        <div className="lg:col-span-2 bg-white rounded-xl shadow-sm border border-gray-100">
          <div className="p-6 border-b border-gray-100">
            <h2 className="text-lg font-semibold text-gray-900">Recent Activity</h2>
          </div>
          <div className="p-6">
            <div className="space-y-4">
              {recentActivity.map((activity) => (
                <div key={activity.id} className="flex items-start gap-4 p-4 bg-gray-50 rounded-lg hover:bg-gray-100 transition-colors">
                  <div className="p-2 bg-white rounded-lg shadow-sm">
                    {getStatusIcon(activity.action)}
                  </div>
                  <div className="flex-1">
                    <p className="font-medium text-gray-900">{activity.action}</p>
                    <p className="text-sm text-gray-500 mt-1">{activity.details}</p>
                  </div>
                  <p className="text-sm text-gray-400">{activity.timestamp}</p>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* Quick Actions & Alerts */}
        <div className="space-y-6">
          {/* Quick Actions */}
          <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
            <h2 className="text-lg font-semibold text-gray-900 mb-4">Quick Actions</h2>
            <div className="space-y-3">
              <button className="w-full flex items-center gap-3 p-3 bg-blue-50 text-blue-700 rounded-lg hover:bg-blue-100 transition-colors">
                <Plus className="w-5 h-5" />
                <span className="font-medium">Generate New Key</span>
              </button>
              <button className="w-full flex items-center gap-3 p-3 bg-purple-50 text-purple-700 rounded-lg hover:bg-purple-100 transition-colors">
                <Package className="w-5 h-5" />
                <span className="font-medium">Add Product</span>
              </button>
              <button className="w-full flex items-center gap-3 p-3 bg-green-50 text-green-700 rounded-lg hover:bg-green-100 transition-colors">
                <Download className="w-5 h-5" />
                <span className="font-medium">Export Data</span>
              </button>
            </div>
          </div>

          {/* Alerts */}
          <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
            <h2 className="text-lg font-semibold text-gray-900 mb-4">Alerts</h2>
            <div className="space-y-3">
              <div className="flex items-start gap-3 p-3 bg-yellow-50 border border-yellow-100 rounded-lg">
                <AlertTriangle className="w-5 h-5 text-yellow-600 mt-0.5" />
                <div>
                  <p className="font-medium text-yellow-800">5 Licenses Expiring Soon</p>
          </p className="text-sm text-yellow-600 mt-1">Check the Licenses page to renew</p>
                </div>
              </div>
              <div className="flex items-start gap-3 p-3 bg-red-50 border border-red-100 rounded-lg">
                <XCircle className="w-5 h-5 text-red-600 mt-0.5" />
                <div>
                  <p className="font-medium text-red-800">2 Revoked Licenses</p>
                  <p className="text-sm text-red-600 mt-1">Review your license list</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;
