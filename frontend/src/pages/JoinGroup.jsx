import React, { useState, useEffect } from 'react';
import { getGroups, joinGroup, handleApiError } from '../services/api';
import AlertMessage from '../components/AlertMessage';

export default function JoinGroup({ activeStudent, onMembershipChanged }) {
  const [groups, setGroups] = useState([]);
  const [loading, setLoading] = useState(true);
  const [actionLoadingId, setActionLoadingId] = useState(null);
  const [alert, setAlert] = useState(null);

  useEffect(() => {
    loadGroups();
  }, [activeStudent]);

  const loadGroups = async () => {
    try {
      setLoading(true);
      const data = await getGroups();
      setGroups(data || []);
    } catch (err) {
      setAlert({ type: 'error', message: 'Failed to load study groups: ' + handleApiError(err) });
    } finally {
      setLoading(false);
    }
  };

  const handleJoin = async (groupId, groupName) => {
    if (!activeStudent || !activeStudent.id) {
      setAlert({ type: 'error', message: 'Please select an active student from the top bar before joining.' });
      return;
    }

    try {
      setActionLoadingId(groupId);
      setAlert(null);
      const res = await joinGroup(groupId, activeStudent.id);
      setAlert({
        type: 'success',
        message: `Successfully joined group "${groupName}"! Capacity and member count updated.`,
      });
      // Refresh groups list immediately
      await loadGroups();
      if (onMembershipChanged) onMembershipChanged();
    } catch (err) {
      setAlert({ type: 'error', message: handleApiError(err) });
    } finally {
      setActionLoadingId(null);
    }
  };

  return (
    <div>
      <div className="section-header">
        <h2>Compulsory Feature 2: Join Group</h2>
        <p>
          Browse active study groups and join an open slot. Capacity is strictly enforced via database locking.
        </p>
      </div>

      <AlertMessage
        type={alert?.type}
        message={alert?.message}
        onClose={() => setAlert(null)}
      />

      {loading ? (
        <div className="loading-container">
          <div className="spinner" />
          <p>Fetching active study groups...</p>
        </div>
      ) : groups.length === 0 ? (
        <div className="empty-state">
          <div className="empty-state-icon">👥</div>
          <h3>No study groups available</h3>
          <p>There are no study groups created yet. Use the Create Group tab to start the first group!</p>
        </div>
      ) : (
        <div className="cards-grid">
          {groups.map((group) => {
            const isFull = group.currentMemberCount >= group.maxMembers;
            const percentFilled = Math.min(100, (group.currentMemberCount / group.maxMembers) * 100);
            const isCreator = activeStudent && group.creatorId === activeStudent.id;

            return (
              <div key={group.id} className="group-card">
                <div>
                  <span className="subject-badge">
                    {group.subjectCode} • {group.subjectName}
                  </span>
                  <div className="group-card-header">
                    <h3 className="group-card-title">{group.groupName}</h3>
                  </div>

                  <div className="group-card-meta">
                    <div className="meta-row">
                      <span>Creator</span>
                      <span style={{ fontWeight: 600 }}>
                        {group.creatorName} {isCreator ? '(You)' : ''}
                      </span>
                    </div>
                    <div className="meta-row">
                      <span>Member Capacity</span>
                      <span style={{ fontWeight: 600 }}>
                        {group.currentMemberCount} / {group.maxMembers}
                      </span>
                    </div>
                    <div className="meta-row">
                      <span>Available Slots</span>
                      <span
                        style={{
                          fontWeight: 700,
                          color: isFull ? '#ef4444' : '#10b981',
                        }}
                      >
                        {isFull ? 'Group Full' : `${group.availableSpaces} open`}
                      </span>
                    </div>
                  </div>

                  <div className="capacity-container">
                    <div className="capacity-label">
                      <span>Occupancy</span>
                      <span>{Math.round(percentFilled)}%</span>
                    </div>
                    <div className="progress-track">
                      <div
                        className={`progress-fill ${
                          isFull ? 'full' : percentFilled >= 75 ? 'warn' : 'safe'
                        }`}
                        style={{ width: `${percentFilled}%` }}
                      />
                    </div>
                  </div>
                </div>

                <div style={{ marginTop: '16px' }}>
                  <button
                    id={`btn-join-group-${group.id}`}
                    type="button"
                    className="btn btn-primary"
                    style={{ width: '100%' }}
                    onClick={() => handleJoin(group.id, group.groupName)}
                    disabled={isFull || actionLoadingId === group.id}
                  >
                    {actionLoadingId === group.id ? (
                      <>
                        <span className="spinner" /> Joining...
                      </>
                    ) : isFull ? (
                      'Group Full'
                    ) : (
                      `Join Group as ${activeStudent ? activeStudent.name.split(' ')[0] : 'Student'}`
                    )}
                  </button>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}
