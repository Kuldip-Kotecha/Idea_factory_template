import { SecureLinksPipe } from './secure-links.pipe';

describe('SecureLinksPipe', () => {
  let pipe: SecureLinksPipe;

  beforeEach(() => {
    pipe = new SecureLinksPipe();
  });

  it('create an instance', () => {
    expect(pipe).toBeTruthy();
  });

  it('should add rel="noopener noreferrer" to target="_blank" links', () => {
    const input = '<a href="https://example.com" target="_blank">Link</a>';
    const output = pipe.transform(input);
    expect(output).toContain('target="_blank" rel="noopener noreferrer"');
  });

  it('should append to existing rel attribute', () => {
    const input = '<a href="x" target="_blank" rel="nofollow">Link</a>';
    const output = pipe.transform(input);
    expect(output).toContain('rel="nofollow noopener noreferrer"');
    // Verify it doesn't create duplicate rel attributes
    const relCount = (output.match(/rel=/g) || []).length;
    expect(relCount).toBe(1);
  });

  it('should handle single quotes', () => {
    const input = "<a href='https://example.com' target='_blank'>Link</a>";
    const output = pipe.transform(input);
    expect(output).toContain("rel=\"noopener noreferrer\"");
  });

  it('should not modify links without target="_blank"', () => {
    const input = '<a href="https://example.com">Link</a>';
    const output = pipe.transform(input);
    expect(output).toBe(input);
  });

  it('should handle multiple links', () => {
    const input = '<a href="1" target="_blank">1</a><a href="2">2</a><a href="3" target="_blank">3</a>';
    const output = pipe.transform(input);
    const matches = output.match(/rel="[^"]*noopener noreferrer[^"]*"/g);
    expect(matches?.length).toBe(2);
  });
});
