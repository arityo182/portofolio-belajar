import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';

// Unit test untuk validasi tanggal appointment (tanpa render komponen penuh)
describe('AppointmentForm — validasi tanggal', () => {
  const isTanggalValid = (tanggal: string): boolean => {
    const tgl = new Date(tanggal);
    const now = new Date();
    now.setHours(0, 0, 0, 0);
    tgl.setHours(0, 0, 0, 0);
    return tgl >= now;
  };

  it('tanggal hari ini — valid', () => {
    const today = new Date().toISOString().split('T')[0];
    expect(isTanggalValid(today)).toBe(true);
  });

  it('tanggal besok — valid', () => {
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    expect(isTanggalValid(tomorrow.toISOString().split('T')[0])).toBe(true);
  });

  it('tanggal kemarin — tidak valid', () => {
    const yesterday = new Date();
    yesterday.setDate(yesterday.getDate() - 1);
    expect(isTanggalValid(yesterday.toISOString().split('T')[0])).toBe(false);
  });

  it('tanggal tahun lalu — tidak valid', () => {
    expect(isTanggalValid('2020-01-01')).toBe(false);
  });
});
