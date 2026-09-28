import React, { useState, useEffect } from 'react';
import {
  getStudentJoinedGroups,
  leaveGroup,
  getGroupMembers,
  transferCreator,
  handleApiError,
} from '../services/api';
import AlertMessage from '../components/AlertMessage';

export default function LeaveGroup({ activeStudent, onMembershipChanged }) {
  const [joinedGroups, setJoinedGroups] = useState([]);
  const [loading, setLoading] = useState(true);
  const [actionLoadingId, setActionLoadingId] = useState(null);
  const [alert, setAlert] = useState(null);

  // Transfer ownership state
  const [transferModalGroup, setTransferModalGroup] = useState(null);
  const [groupMembers, setGroupMembers] = useState([]);
  const [selectedNewCreatorId, setSelectedNewCreatorId] = useState('');
  const [transferring, setTransferring] = useState(false);

  useEffect(() => {
    if (activeStudent && activeStudent.id) {
      loadJoinedGroups();
    } else {
      setJoinedGroups([]);
      setLoading(false);
    }
  }, [activeStudent]);

  const loadJoinedGroups = async () => {
    try {
      setLoading(true);
      const data = await getStudentJoinedGroups(activeStudent.id);
      setJoinedGroups(data || []);
    } catch (err) {
      setAlert({
        type: 'error',
        message: 'Failed to load your joined groups: ' + handleApiError(err),
      });
    } finally {
      setLoading(false);
    }
  };

  const handleLeave = async (group) => {
    if (!activeStudent || !activeStudent.id) {
      setAlert({ type: 'error', message: 'No active student selected.' });
      return;
    }

    // Check if current user is the creator
    const isCreator = group.creatorId === activeStudent.id;
    if (isCreator) {
      setAlert({
        type: 'error',
        message: `As the creator of "${group.groupName}", you cannot leave directly. Please transfer creator ownership to another member first.`,
      });
      // Automatically open transfer modal
      openTransferModal(group);
      return;
    }

    try {
      setActionLoadingId(group.id);
      setAlert(null);
      await leaveGroup(group.id, activeStudent.id);
      setAlert({
        type: 'success',
        message: `You have successfully left the study group "${group.groupName}".`,
      });
      await loadJoinedGroups();
      if (onMembershipChanged) onMembershipChanged();
    } catch (err) {
      setAlert({ type: 'error', message: handleApiError(err) });
    } finally {
      setActionLoadingId(null);
    }
  };

  const openTransferModal = async (group) => {
    try {
      setTransferModalGroup(group);
      const members = await getGroupMembers(group.id);
      // Filter out the creator
      const otherMembers = (members || []).filter((m) => m.studentId !== activeStudent.id);
      setGroupMembers(otherMembers);
      if (otherMembers.length > 0) {
        setSelectedNewCreatorId(otherMembers[0].studentId.toString());
      } else {
        setSelectedNewCreatorId('');
      }
    } catch (err) {
      setAlert({ type: 'error', message: 'Could not load group members: ' + handleApiError(err) });
    }
  };

  const handleTransferOwnership = async (e) => {
    e.preventDefault();
    if (!selectedNewCreatorId) {
      setAlert({ type: 'error', message: 'Please select a member to transfer ownership to.' });
      return;
    }

    try {
      setTransferring(true);
      await transferCreator(transferModalGroup.id, parseInt(selectedNewCreatorId, 10));
      setAlert({
        type: 'success',
        message: `Creator ownership of "${transferModalGroup.groupName}" transferred successfully! You are now a regular member and can leave the group.`,
      });
      setTransferModalGroup(null);
      await loadJoinedGroups();
      if (onMembershipChanged) onMembershipChanged();
    } catch (err) {
      setAlert({ type: 'error', message: handleApiError(err) });
    } finally {
      setTransferring(false);
    }
  };

  return (
    <div>
      <div className="section-header">
        <h2>Compulsory Feature 3: Leave Group</h2>
        <p>
          View study groups you have joined and leave anytime. Group creators must transfer ownership before departing.
        </p>
      </div>

      <AlertMessage
        type={alert?.type}
        message={alert?.message}
        onClose={() => setAlert(null)}
      />

      {/* Transfer Ownership Modal / Card */}
      {transferModalGroup && (
        <div
          style={{
            background: '#f5f3ff',
            border: '2px solid #8b5cf6',
            borderRadius: '12px',
            padding: '24px',
            marginBottom: '24px',
          }}
        >
          <h3 style={{ color: '#5b21b6', marginBottom: '8px' }}>
            👑 Transfer Creator Ownership: {transferModalGroup.groupName}
          </h3>
          <p style={{ fontSize: '0.9rem', color: '#4c1d95', marginBottom: '16px' }}>
            Under the university assessment rules, a study group cannot be left without an active creator.
            Select an enrolled member to designate as the new creator:
          </p>

          {groupMembers.length === 0 ? (
            <p style={{ color: '#b91c1c', fontWeight: 600 }}>
              No other members in this group to transfer ownership to.
            </p>
          ) : (
            <form onSubmit={handleTransferOwnership} style={{ display: 'flex', gap: '12px', flexWrap: 'wrap' }}>
              <select
                className="form-control"
                style={{ flex: 1, minWidth: '240px' }}
                value={selectedNewCreatorId}
                onChange={(e) => setSelectedNewCreatorId(e.target.value)}
                disabled={transferring}
              >
                {groupMembers.map((m) => (
                  <option key={m.studentId} value={m.studentId}>
                    {m.studentName} ({m.studentEmail})
                  </option>
                ))}
              </select>

              <button type="submit" className="btn btn-primary" disabled={transferring}>
                {transferring ? 'Transferring...' : 'Confirm Transfer'}
              </button>

              <button
                type="button"
                className="btn btn-outline"
                onClick={() => setTransferModalGroup(null)}
              >
                Cancel
              </button>
            </form>
          )}
        </div>
      )}

      {loading ? (
        <div className="loading-container">
          <div className="spinner" />
          <p>Loading your study group memberships...</p>
        </div>
      ) : joinedGroups.length === 0 ? (
        <div className="empty-state">
          <div className="empty-state-icon">📖</div>
          <h3>No joined groups for {activeStudent ? activeStudent.name : 'this student'}</h3>
          <p>
            You have not joined any study groups yet. Go to the Join Group tab to discover and join available groups!
          </p>
        </div>
      ) : (
        <div className="cards-grid">
          {joinedGroups.map((group) => {
            const isCreator = group.creatorId === activeStudent?.id;
            return (
              <div key={group.id} className="group-card">
                <div>
                  <span className="subject-badge">
                    {group.subjectCode} • {group.subjectName}
                  </span>
                  <div className="group-card-header">
                    <h3 className="group-card-title">{group.groupName}</h3>
                    {isCreator && <span className="creator-badge">Creator</span>}
                  </div>

                  <div className="group-card-meta">
                    <div className="meta-row">
                      <span>Group Creator</span>
                      <span style={{ fontWeight: 600 }}>
                        {group.creatorName} {isCreator ? '(You)' : ''}
                      </span>
                    </div>
                    <div className="meta-row">
                      <span>Members</span>
                      <span style={{ fontWeight: 600 }}>
                        {group.currentMemberCount} / {group.maxMembers}
                      </span>
                    </div>
                    <div className="meta-row">
                      <span>Created On</span>
                      <span style={{ fontSize: '0.82rem' }}>
                        {group.createdAt ? new Date(group.createdAt).toLocaleDateString() : 'Active'}
                      </span>
                    </div>
                  </div>
                </div>

                <div style={{ marginTop: '16px', display: 'flex', gap: '8px' }}>
                  <button
                    id={`btn-leave-group-${group.id}`}
                    type="button"
                    className="btn btn-danger"
                    style={{ flex: 1 }}
                    onClick={() => handleLeave(group)}
                    disabled={actionLoadingId === group.id}
                  >
                    {actionLoadingId === group.id ? (
                      <>
                        <span className="spinner" /> Leaving...
                      </>
                    ) : (
                      'Leave Group'
                    )}
                  </button>

                  {isCreator && (
                    <button
                      type="button"
                      className="btn btn-outline"
                      title="Transfer ownership to another member before leaving"
                      onClick={() => openTransferModal(group)}
                    >
                      👑 Transfer
                    </button>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}
