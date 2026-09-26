import { MapContainer, Marker, TileLayer, useMapEvents } from 'react-leaflet';
import './leafletIconFix';

// Rushinga, Mashonaland Central - a sensible default centre for a
// province-wide disaster monitoring system.
const DEFAULT_CENTER = [-16.71, 32.18];

function ClickHandler({ onPick }) {
  useMapEvents({
    click(e) {
      onPick(e.latlng.lat, e.latlng.lng);
    },
  });
  return null;
}

/** Click-to-set-coordinates widget for the incident submission form. */
export default function MapPicker({ latitude, longitude, onChange }) {
  const position = latitude != null && longitude != null ? [latitude, longitude] : null;

  return (
    <div className="overflow-hidden rounded-lg border border-slate-200">
      <MapContainer
        center={position || DEFAULT_CENTER}
        zoom={position ? 13 : 9}
        style={{ height: '220px', width: '100%' }}
      >
        <TileLayer
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        />
        <ClickHandler onPick={onChange} />
        {position && <Marker position={position} />}
      </MapContainer>
      <p className="bg-slate-50 px-3 py-1.5 text-xs text-slate-500">
        Click on the map to set this incident's GPS coordinates at ward level.
      </p>
    </div>
  );
}
