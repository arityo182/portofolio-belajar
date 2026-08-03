import { describe, it, expect, vi } from 'vitest';
import axios from 'axios';

vi.mock('axios');

describe('Auth Service', () => {
  const mockLogin = async (email: string, password: string) => {
    try {
      const res = await axios.post('/api/auth/login', { email, password });
      return res.data;
    } catch {
      throw new Error('Login failed');
    }
  };

  it('login berhasil mengembalikan token', async () => {
    const mockResponse = { data: { token: 'jwt-token-123', user: { nama: 'Admin', email: 'admin@medikasentosa.id', role: 'ADMIN' } } };
    vi.mocked(axios.post).mockResolvedValueOnce(mockResponse);

    const result = await mockLogin('admin@medikasentosa.id', 'arie12345');
    expect(result.token).toBe('jwt-token-123');
    expect(result.user.role).toBe('ADMIN');
  });

  it('login gagal — password salah throw error', async () => {
    vi.mocked(axios.post).mockRejectedValueOnce({ response: { status: 404, data: { message: 'Email atau password salah' } } });

    await expect(mockLogin('admin@medikasentosa.id', 'wrong')).rejects.toThrow('Login failed');
  });

  it('login gagal — akun nonaktif throw error', async () => {
    vi.mocked(axios.post).mockRejectedValueOnce({ response: { status: 403, data: { message: 'Akun dinonaktifkan' } } });

    await expect(mockLogin('blocked@medika.com', 'pass')).rejects.toThrow('Login failed');
  });
});
