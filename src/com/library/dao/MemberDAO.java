package com.library.dao;

import com.library.model.Member;
import com.library.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

/** DAO for the 'members' table. Same structure as BookDAO. */
public class MemberDAO {

    private static final String SELECT_ALL = "SELECT member_id, name, email, contact FROM members";

    // ------------------------- CREATE -------------------------
    public void addMember(Member m) throws SQLException {
        String sql = "INSERT INTO members (member_id, name, email, contact) VALUES (?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, m.getMemberId());
            ps.setString(2, m.getName());
            ps.setString(3, m.getEmail());
            ps.setString(4, m.getContact());
            ps.executeUpdate();
        }
    }

    // -------------------------- READ --------------------------
    public ArrayList<Member> getAllMembers() throws SQLException {
        ArrayList<Member> members = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_ALL + " ORDER BY member_id");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                members.add(mapRow(rs));
            }
        }
        return members;
    }

    /** memberId -> Member, for fast lookup. */
    public HashMap<Integer, Member> getMemberMap() throws SQLException {
        HashMap<Integer, Member> map = new HashMap<>();
        for (Member m : getAllMembers()) {
            map.put(m.getMemberId(), m);
        }
        return map;
    }

    /** Returns one member or null if not found. */
    public Member getMemberById(int memberId) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_ALL + " WHERE member_id = ?")) {
            ps.setInt(1, memberId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    // ------------------------ SEARCH (overloaded) -------------
    public ArrayList<Member> searchMembers(int memberId) throws SQLException {
        ArrayList<Member> result = new ArrayList<>();
        Member m = getMemberById(memberId);
        if (m != null) {
            result.add(m);
        }
        return result;
    }

    /** Search by part of name or email. */
    public ArrayList<Member> searchMembers(String keyword) throws SQLException {
        ArrayList<Member> result = new ArrayList<>();
        String sql = SELECT_ALL + " WHERE name LIKE ? OR email LIKE ? ORDER BY member_id";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");
            ps.setString(2, "%" + keyword + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
        }
        return result;
    }

    // ------------------------ UPDATE --------------------------
    public boolean updateMember(Member m) throws SQLException {
        String sql = "UPDATE members SET name = ?, email = ?, contact = ? WHERE member_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, m.getName());
            ps.setString(2, m.getEmail());
            ps.setString(3, m.getContact());
            ps.setInt(4, m.getMemberId());
            return ps.executeUpdate() > 0;
        }
    }

    // ------------------------ DELETE --------------------------
    public boolean deleteMember(int memberId) throws SQLException {
        String sql = "DELETE FROM members WHERE member_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, memberId);
            return ps.executeUpdate() > 0;
        }
    }

    private Member mapRow(ResultSet rs) throws SQLException {
        return new Member(
                rs.getInt("member_id"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getString("contact"));
    }
}
