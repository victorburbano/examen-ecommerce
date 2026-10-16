import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CapAlert } from './cap-alert';

describe('CapAlert', () => {
  async function create(visible: boolean): Promise<ComponentFixture<CapAlert>> {
    TestBed.configureTestingModule({ imports: [CapAlert] });
    const fixture = TestBed.createComponent(CapAlert);
    fixture.componentRef.setInput('visible', visible);
    await fixture.whenStable();
    return fixture;
  }

  it('shows nothing when the cap does not apply', async () => {
    const fixture = await create(false);

    expect(fixture.nativeElement.querySelector('.cap')).toBeNull();
    expect(fixture.nativeElement.textContent.trim()).toBe('');
  });

  it('shows the exact cap message when it applies', async () => {
    const fixture = await create(true);

    const banner = fixture.nativeElement.querySelector('.cap') as HTMLElement;
    expect(banner).not.toBeNull();
    expect(banner.textContent?.trim()).toBe(
      '¡Enhorabuena! Has alcanzado el límite máximo de ahorro permitido (35%)',
    );
  });

  it('is an accessible live status region', async () => {
    const fixture = await create(true);

    const banner = fixture.nativeElement.querySelector('.cap') as HTMLElement;
    expect(banner.getAttribute('role')).toBe('status');
    expect(banner.getAttribute('aria-live')).toBe('polite');
  });
});
