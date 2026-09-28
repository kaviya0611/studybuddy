import React, { useState, useEffect } from 'react';
import { getStudents, loginStudent, handleApiError } from './services/api';
import CreateGroup from './pages/CreateGroup';
import JoinGroup from './pages/JoinGroup';
import LeaveGroup from './pages/LeaveGroup';
import ViewGroupsBySubject from './pages/ViewGroupsBySubject';
import RemoveMember from './pages/RemoveMember';

export default function App() {
  const [activeTab, setActiveTab] = useState('create');
  const [students, setStudents] = useState([]);
  const [activeStudent, setActiveStudent] = useState(null);
  const [loadingStudents, setLoadingStudents] = useState(true);
  const [refreshKey, setRefreshKey] = useState(0);

  useEffect(() => {
    loadStudents();
  }, []);

  const loadStudents = async () => {
    try {
      setLoadingStudents(true);
      const data = await getStudents();
      setStudents(data || []);
      if (data && data.length > 0) {
        // Default to first student
        handleSelectStudent(data[0]);
      }
    } catch (err) {
      console.error('Failed to load students:', err);
    } finally {
      setLoadingStudents(false);
    }
  };

  const handleSelectStudent = async (student) => {
    setActiveStudent(student);
    try {
      const authData = await loginStudent(student.email);
      if (authData && authData.token) {
        localStorage.setItem('studybuddy_auth_token', authData.token);
      }
    } catch (err) {
      // Fallback: store student id as simple token
      localStorage.setItem('studybuddy_auth_token', student.id.toString());
    }
  };

  const triggerRefresh = () => {
    setRefreshKey((prev) => prev + 1);
  };

  const tabs = [
    { id: 'create', label: '1. Create Group', icon: '➕' },
    { id: 'join', label: '2. Join Group', icon: '🤝' },
    { id: 'leave', label: '3. Leave Group', icon: '🚪' },
    { id: 'subject', label: '4. View Groups by Subject', icon: '📚' },
    { id: 'remove', label: '5. Remove Member', icon: '🛡️' },
  ];

  return (
    <div className="app-container">
      {/* Top Banner & Active Student Identity */}
      <header className="app-header">
        <div className="header-brand">
          <h1>
            <span>📘</span> StudyBuddy
          </h1>
          <p>Student Study Group Formation Tool — Assessment Portal</p>
        </div>

        <div className="active-user-card">
          <div className="user-avatar">
            {activeStudent ? activeStudent.name.charAt(0) : '?'}
          </div>
          <div className="user-meta">
            <div className="label">Current Active Student</div>
            <div className="name">{activeStudent ? activeStudent.name : 'Loading...'}</div>
            <div className="email">{activeStudent ? activeStudent.email : ''}</div>
          </div>
          <div className="user-selector" style={{ marginLeft: '12px' }}>
            <select
              aria-label="Switch active student"
              value={activeStudent?.id || ''}
              onChange={(e) => {
                const found = students.find((s) => s.id.toString() === e.target.value);
                if (found) handleSelectStudent(found);
              }}
              disabled={loadingStudents}
            >
              {students.map((s) => (
                <option key={s.id} value={s.id}>
                  Switch to: {s.name}
                </option>
              ))}
            </select>
          </div>
        </div>
      </header>

      {/* 5 Compulsory Feature Navigation Tabs */}
      <nav className="nav-tabs" aria-label="Feature Tabs">
        {tabs.map((tab) => (
          <button
            key={tab.id}
            id={`tab-${tab.id}`}
            type="button"
            className={`nav-tab-btn ${activeTab === tab.id ? 'active' : ''}`}
            onClick={() => setActiveTab(tab.id)}
          >
            <span>{tab.icon}</span>
            <span>{tab.label}</span>
          </button>
        ))}
      </nav>

      {/* Main Content Area */}
      <main className="content-panel" key={refreshKey}>
        {activeTab === 'create' && (
          <CreateGroup
            activeStudent={activeStudent}
            onGroupCreated={triggerRefresh}
          />
        )}
        {activeTab === 'join' && (
          <JoinGroup
            activeStudent={activeStudent}
            onMembershipChanged={triggerRefresh}
          />
        )}
        {activeTab === 'leave' && (
          <LeaveGroup
            activeStudent={activeStudent}
            onMembershipChanged={triggerRefresh}
          />
        )}
        {activeTab === 'subject' && (
          <ViewGroupsBySubject />
        )}
        {activeTab === 'remove' && (
          <RemoveMember
            activeStudent={activeStudent}
            onMembershipChanged={triggerRefresh}
          />
        )}
      </main>
    </div>
  );
}
