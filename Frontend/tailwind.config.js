/** @type {import('tailwindcss').Config} */
module.exports = {
    content: [
        "./src/**/*.{html,ts}",
    ],
    theme: {
        extend: {
            colors: {
                indigo: {
                    400: '#818cf8',
                    500: '#6366f1',
                    600: '#4f46e5',
                },
                purple: {
                    500: '#a855f7',
                    600: '#9333ea',
                }
            },
            animation: {
                'float': 'float 6s ease-in-out infinite',
            },
            keyframes: {
                float: {
                    '0%, 100%': { transform: 'translateY(0px)' },
                    '50%': { transform: 'translateY(-10px)' },
                }
            }
        },
    },
    plugins: [
        function({ addBase, addUtilities }) {
            addUtilities({
                '.touch-manipulation': {
                    'touch-action': 'manipulation',
                },
                '.gpu': {
                    'transform': 'translateZ(0)',
                    'will-change': 'transform',
                    'backface-visibility': 'hidden',
                },
            });
        }
    ],
}
