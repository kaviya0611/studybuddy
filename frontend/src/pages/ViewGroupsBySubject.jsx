import React, { useState, useEffect } from 'react';
import { getSubjects, getGroups, handleApiError } from '../services/api';
import AlertMessage from '../components/AlertMessage';

export default function ViewGroupsBySubject() {
  const [subjects, setSubjects] = useState([]);
  const [selectedSubjectId, setSelectedSubjectId] = useState('');
  const [groups, setGroups] = useState([]);
  const [loadingSubjects, setLoadingSubjects] = useState(true);
  const [loadingGroups, setLoadingGroups] = useState(false);
  const [alert, setAlert] = useState(null);

  useEffect(() => {
    loadSubjects();
  }, []);

  useEffect(() => {
    if (selectedSubjectId) {
      loadGroupsBySubject(selectedSubjectId);
    } else {
      setGroups([]);
    }
  }, [selectedSubjectId]);

  const loadSubjects = async () => {
    try {
      setLoadingSubjects(true);
      const data = await getSubjects();
      setSubjects(data || []);
      if (data && data.length > 0) {
        setSelectedSubjectId(data[0].id.toString());
      }
    } catch (err) {
      setAlert({
        type: 'error',
        message: 'Failed to load subjects: ' + handleApiError(err),
      });
    } finally {
      setLoadingSubjects(false);
    }
  };

  const loadGroupsBySubject = async (subjectId) => {
    try {
      setLoadingGroups(true);
      setAlert(null);
      const data = await getGroups(subjectId);
      setGroups(data || []);
    } catch (err) {
      setAlert({
        type: 'error',
        message: 'Failed to load subject groups: ' + handleApiError(err),
      });
    } finally {
      setLoadingGroups(false);
    }
  };

  const selectedSubject = subjects.find(
    (s) => s.id.toString() === selectedSubjectId
  );

  return (
    <div>
      <div className="section-header">
        <h2>Compulsory Feature 4: View Groups by Subject</h2>
        <p>Filter active study groups by curriculum subject to find exam study partners.</p>
      </div>

      <AlertMessage
        type={alert?.type}
        message={alert?.message}
        onClose={() => setAlert(null)}
      />

      <div style={{ maxWidth: '520px', marginBottom: '28px' }}>
        <label className="form-label" htmlFor="subjectFilterSelect">
          Select Subject to Filter
        </label>
        <select
          id="subjectFilterSelect"
          className="form-control"
          value={selectedSubjectId}
          onChange={(e) => setSelectedSubjectId(e.target.value)}
          disabled={loadingSubjects}
        >
          <option value="">-- Choose a Subject --</option>
          {subjects.map((sub) => (
            <option key={sub.id} value={sub.id}>
              {sub.subjectCode} - {sub.subjectName}
            </option>
          ))}
        </select>
      </div>

      {loadingSubjects || loadingGroups ? (
        <div className="loading-container">
          <div className="spinner" />
          <p>Fetching study groups for {selectedSubject ? selectedSubject.subjectName : 'subject'}...</p>
        </div>
      ) : !selectedSubjectId ? (
        <div className="empty-state">
          <div className="empty-state-icon">🔍</div>
          <h3>Select a subject above</h3>
          <p>Choose any subject from the dropdown to see all its registered study groups.</p>
        </div>
      ) : groups.length === 0 ? (
        <div className="empty-state">
          <div className="empty-state-icon">📚</div>
          <h3>No study groups found for {selectedSubject?.subjectName} ({selectedSubject?.subjectCode})</h3>
          <p>There are currently no active study groups for this subject. Create one in the Create Group tab!</p>
        </div>
      ) : (
        <div>
          <div style={{ marginBottom: '16px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <h3 style={{ fontSize: '1.1rem', fontWeight: 600 }}>
              Found {groups.length} group{groups.length > 1 ? 's' : ''} in {selectedSubject?.subjectName}
            </h3>
          </div>

          <div className="table-responsive" style={{ background: 'white', borderRadius: '12px', border: '1px solid #e2e8f0', boxShadow: 'var(--shadow-sm)' }}>
            <table className="data-table">
              <thead>
                <tr>
                  <th>Group Name</th>
                  <th>Subject</th>
                  <th>Creator</th>
                  <th>Current Members</th>
                  <th>Max Capacity</th>
                  <th>Available Spaces</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {groups.map((group) => {
                  const isFull = group.currentMemberCount >= group.maxMembers;
                  return (
                    <tr key={group.id}>
                      <td style={{ fontWeight: 600, color: '#312e81' }}>{group.groupName}</td>
                      <td>
                        <span className="subject-badge" style={{ marginBottom: 0 }}>
                          {group.subjectCode}
                        </span>
                      </td>
                      <td>{group.creatorName}</td>
                      <td style={{ fontWeight: 600 }}>{group.currentMemberCount}</td>
                      <td>{group.maxMembers}</td>
                      <td style={{ fontWeight: 700, color: isFull ? '#ef4444' : '#10b981' }}>
                        {group.availableSpaces} spots
                      </td>
                      <td>
                        <span
                          style={{
                            display: 'inline-block',
                            padding: '3px 10px',
                            borderRadius: '9999px',
                            fontSize: '0.78rem',
                            fontWeight: 700,
                            background: isFull ? '#fee2e2' : '#dcfce7',
                            color: isFull ? '#991b1b' : '#166534',
                          }}
                        >
                          {isFull ? 'FULL' : 'OPEN'}
                        </span>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
}
