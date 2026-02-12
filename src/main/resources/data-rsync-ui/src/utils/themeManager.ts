// 主题管理工具

export type ThemeMode = 'light' | 'dark';

// 主题配置
const themeConfig = {
  light: {
    '--primary-color': '#409eff',
    '--success-color': '#67c23a',
    '--warning-color': '#e6a23c',
    '--danger-color': '#f56c6c',
    '--info-color': '#909399',
    '--background-color': '#ffffff',
    '--text-color': '#303133',
    '--text-color-secondary': '#606266',
    '--border-color': '#dcdfe6',
    '--border-color-light': '#ebeef5',
    '--border-color-lighter': '#f0f2f5',
    '--box-shadow': '0 2px 12px 0 rgba(0, 0, 0, 0.1)',
    '--box-shadow-light': '0 2px 8px rgba(0, 0, 0, 0.08)',
    '--box-shadow-dark': '0 2px 12px 0 rgba(0, 0, 0, 0.15)'
  },
  dark: {
    '--primary-color': '#409eff',
    '--success-color': '#67c23a',
    '--warning-color': '#e6a23c',
    '--danger-color': '#f56c6c',
    '--info-color': '#909399',
    '--background-color': '#0f172a',
    '--text-color': '#f8fafc',
    '--text-color-secondary': '#94a3b8',
    '--border-color': 'rgba(59, 130, 246, 0.2)',
    '--border-color-light': 'rgba(59, 130, 246, 0.1)',
    '--border-color-lighter': 'rgba(59, 130, 246, 0.05)',
    '--box-shadow': '0 2px 12px 0 rgba(0, 0, 0, 0.3)',
    '--box-shadow-light': '0 2px 8px rgba(0, 0, 0, 0.2)',
    '--box-shadow-dark': '0 2px 12px 0 rgba(0, 0, 0, 0.4)'
  }
};

// 主题管理器
class ThemeManager {
  private currentTheme: ThemeMode;
  private themeChangeCallbacks: Array<(theme: ThemeMode) => void> = [];

  constructor() {
    // 从本地存储读取主题，默认深色模式
    const savedTheme = localStorage.getItem('theme') as ThemeMode | null;
    this.currentTheme = savedTheme || 'dark';
    this.applyTheme(this.currentTheme);
  }

  // 应用主题
  private applyTheme(theme: ThemeMode) {
    const root = document.documentElement;
    
    // 设置主题类
    root.classList.remove('light-theme', 'dark-theme');
    root.classList.add(`${theme}-theme`);
    
    // 应用主题变量
    const variables = themeConfig[theme];
    Object.entries(variables).forEach(([key, value]) => {
      root.style.setProperty(key, value);
    });
    
    // 保存到本地存储
    localStorage.setItem('theme', theme);
    
    // 触发回调
    this.themeChangeCallbacks.forEach(callback => callback(theme));
  }

  // 切换主题
  toggleTheme(): ThemeMode {
    this.currentTheme = this.currentTheme === 'light' ? 'dark' : 'light';
    this.applyTheme(this.currentTheme);
    return this.currentTheme;
  }

  // 设置主题
  setTheme(theme: ThemeMode): void {
    if (theme !== this.currentTheme) {
      this.currentTheme = theme;
      this.applyTheme(this.currentTheme);
    }
  }

  // 获取当前主题
  getTheme(): ThemeMode {
    return this.currentTheme;
  }

  // 监听主题变化
  onThemeChange(callback: (theme: ThemeMode) => void): () => void {
    this.themeChangeCallbacks.push(callback);
    
    // 返回取消监听函数
    return () => {
      this.themeChangeCallbacks = this.themeChangeCallbacks.filter(cb => cb !== callback);
    };
  }
}

// 导出单例
const themeManager = new ThemeManager();
export default themeManager;
